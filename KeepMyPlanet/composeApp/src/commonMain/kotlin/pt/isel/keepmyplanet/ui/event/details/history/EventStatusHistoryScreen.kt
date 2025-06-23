package pt.isel.keepmyplanet.ui.event.details.history

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import pt.isel.keepmyplanet.domain.common.Id
import pt.isel.keepmyplanet.dto.event.EventStateChangeResponse
import pt.isel.keepmyplanet.ui.components.AppTopBar
import pt.isel.keepmyplanet.ui.components.ErrorState
import pt.isel.keepmyplanet.ui.components.FullScreenLoading
import pt.isel.keepmyplanet.ui.event.list.components.EmptyState

@Composable
fun EventStatusHistoryScreen(
    viewModel: EventStatusHistoryViewModel,
    eventId: Id,
    onNavigateBack: () -> Unit,
) {
    val uiState by viewModel.uiState.collectAsState()

    LaunchedEffect(eventId) {
        viewModel.loadHistory(eventId)
    }

    Scaffold(
        topBar = {
            AppTopBar(
                title = "Status History",
                onNavigateBack = onNavigateBack,
            )
        },
    ) { paddingValues ->
        Box(modifier = Modifier.fillMaxSize().padding(paddingValues)) {
            when {
                uiState.isLoading -> FullScreenLoading()
                uiState.error != null -> ErrorState(message = uiState.error!!) {
                    viewModel.loadHistory(eventId)
                }
                uiState.history.isEmpty() -> EmptyState(message = "No status changes have been recorded.")
                else -> {
                    LazyColumn(
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

@Composable
private fun StatusHistoryItem(item: EventStateChangeResponse) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = 2.dp,
    ) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Text(
                text = "Changed to: ${item.newStatus}",
                style = MaterialTheme.typography.h6,
            )
            Text(
                text = "Changed by: ${item.changedBy.name}",
                style = MaterialTheme.typography.body2,
            )
            Text(
                text = item.changeTime,
                style = MaterialTheme.typography.caption,
                color = MaterialTheme.colors.onSurface.copy(alpha = 0.7f),
            )
        }
    }
}
