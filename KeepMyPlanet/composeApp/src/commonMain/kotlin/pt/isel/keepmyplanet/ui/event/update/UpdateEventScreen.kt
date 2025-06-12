package pt.isel.keepmyplanet.ui.event.update

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Scaffold
import androidx.compose.material.SnackbarDuration
import androidx.compose.material.SnackbarHost
import androidx.compose.material.SnackbarHostState
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.flow.collectLatest
import pt.isel.keepmyplanet.ui.components.AppTopBar
import pt.isel.keepmyplanet.ui.components.FullScreenLoading
import pt.isel.keepmyplanet.ui.components.LoadingButton
import pt.isel.keepmyplanet.ui.event.EventViewModel
import pt.isel.keepmyplanet.ui.event.components.EventForm
import pt.isel.keepmyplanet.ui.event.model.EventScreenEvent

@Suppress("ktlint:standard:function-naming")
@Composable
fun UpdateEventScreen(
    viewModel: EventViewModel,
    eventId: UInt,
    onNavigateBack: () -> Unit,
) {
    val detailsState by viewModel.detailsUiState.collectAsState()
    val formUiState by viewModel.formUiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(eventId) {
        viewModel.loadEventDetails(eventId)
    }

    LaunchedEffect(detailsState.event) {
        if (detailsState.event?.id?.value == eventId) {
            viewModel.prepareFormForEdit()
        }
    }

    LaunchedEffect(viewModel.events) {
        viewModel.events.collectLatest { event ->
            when (event) {
                is EventScreenEvent.NavigateBack -> onNavigateBack()
                is EventScreenEvent.ShowSnackbar ->
                    snackbarHostState.showSnackbar(event.message, duration = SnackbarDuration.Short)

                else -> {}
            }
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = { AppTopBar(title = "Edit Event", onNavigateBack = onNavigateBack) },
    ) { paddingValues ->
        when {
            detailsState.isLoading -> {
                FullScreenLoading(modifier = Modifier.padding(paddingValues))
            }

            detailsState.error != null -> {
                Box(
                    modifier = Modifier.fillMaxSize().padding(paddingValues),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(text = detailsState.error!!, color = MaterialTheme.colors.error)
                }
            }

            detailsState.event != null -> {
                Column(
                    modifier = Modifier.fillMaxSize().padding(paddingValues).padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                ) {
                    EventForm(
                        formUiState = formUiState,
                        onTitleChanged = viewModel::onTitleChanged,
                        onDescriptionChanged = viewModel::onDescriptionChanged,
                        onStartDateChanged = viewModel::onStartDateChanged,
                        onMaxParticipantsChanged = viewModel::onMaxParticipantsChanged,
                        onZoneIdChanged = { },
                        showZoneIdField = false,
                    )

                    Spacer(modifier = Modifier.weight(1f))

                    LoadingButton(
                        onClick = { viewModel.updateEvent(eventId) },
                        modifier = Modifier.fillMaxWidth(),
                        isLoading = formUiState.isSubmitting,
                        enabled =
                            !formUiState.isSubmitting &&
                                formUiState.title.isNotBlank() &&
                                formUiState.description.isNotBlank() &&
                                formUiState.startDate.isNotBlank(),
                        text = "Confirm",
                    )
                }
            }
        }
    }
}
