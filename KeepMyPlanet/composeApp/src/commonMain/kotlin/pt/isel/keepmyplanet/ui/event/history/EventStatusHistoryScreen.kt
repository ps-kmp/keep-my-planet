package pt.isel.keepmyplanet.ui.event.history

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
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
import pt.isel.keepmyplanet.domain.common.Id
import pt.isel.keepmyplanet.navigation.rememberSavableLazyListState
import pt.isel.keepmyplanet.ui.components.AppTopBar
import pt.isel.keepmyplanet.ui.components.EmptyState
import pt.isel.keepmyplanet.ui.components.ErrorState
import pt.isel.keepmyplanet.ui.components.FullScreenLoading
import pt.isel.keepmyplanet.ui.event.history.components.StatusHistoryItem
import pt.isel.keepmyplanet.ui.event.history.states.EventStatusHistoryEvent

@Composable
fun EventStatusHistoryScreen(
    viewModel: EventStatusHistoryViewModel,
    eventId: Id,
    onNavigateToHome: () -> Unit,
    onNavigateBack: () -> Unit,
    routeKey: String,
) {
    val uiState by viewModel.uiState.collectAsState()
    val listState = rememberSavableLazyListState(key = routeKey)
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(viewModel.events) {
        viewModel.events.collectLatest { event ->
            when (event) {
                is EventStatusHistoryEvent.ShowSnackbar -> {
                    snackbarHostState.showSnackbar(event.message)
                }
            }
        }
    }

    LaunchedEffect(eventId) {
        viewModel.loadHistory(eventId)
    }

    Scaffold(
        topBar = {
            AppTopBar(
                title = "Status History",
                onNavigateBack = onNavigateBack,
                onNavigateToHome = onNavigateToHome,
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) },
    ) { paddingValues ->
        Box(modifier = Modifier.fillMaxSize().padding(paddingValues)) {
            when {
                uiState.isLoading -> FullScreenLoading()
                uiState.error != null ->
                    ErrorState(message = uiState.error!!) {
                        viewModel.loadHistory(eventId)
                    }

                uiState.history.isEmpty() ->
                    EmptyState(message = "No status changes have been recorded.")

                else -> {
                    LazyColumn(
                        state = listState,
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp),
                    ) {
                        items(uiState.history) { historyItem ->
                            StatusHistoryItem(item = historyItem)
                        }
                    }
                }
            }
        }
    }
}
