package pt.isel.keepmyplanet.ui.event.participants

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.DividerDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.flow.collectLatest
import pt.isel.keepmyplanet.ui.components.AppTopBar
import pt.isel.keepmyplanet.ui.components.ErrorState
import pt.isel.keepmyplanet.ui.components.FullScreenLoading
import pt.isel.keepmyplanet.ui.event.details.components.ParticipantRow
import pt.isel.keepmyplanet.ui.event.participants.states.ParticipantListEvent

@Composable
fun ParticipantListScreen(
    viewModel: ParticipantListViewModel,
    onNavigateBack: () -> Unit,
) {
    val uiState by viewModel.uiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(viewModel.events) {
        viewModel.events.collectLatest { event ->
            when (event) {
                is ParticipantListEvent.ShowSnackbar -> {
                    snackbarHostState.showSnackbar(event.message)
                }
            }
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            AppTopBar(
                title = "Event Participants",
                onNavigateBack = onNavigateBack,
            )
        },
    ) { paddingValues ->
        Box(
            modifier = Modifier.fillMaxSize().padding(paddingValues),
        ) {
            when {
                uiState.isLoading -> FullScreenLoading()
                uiState.error != null -> {
                    ErrorState(message = uiState.error!!) {
                        viewModel.loadParticipants()
                    }
                }

                else -> {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(16.dp),
                    ) {
                        items(
                            uiState.participants,
                            key = { it.id.value.toString() },
                        ) { participant ->
                            ParticipantRow(
                                participant = participant,
                                isOrganizer = participant.id == uiState.event?.organizerId,
                            )
                            HorizontalDivider(
                                Modifier.padding(horizontal = 8.dp),
                                DividerDefaults.Thickness,
                                DividerDefaults.color,
                            )
                        }
                    }
                }
            }
        }
    }
}
