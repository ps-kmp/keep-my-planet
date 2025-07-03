package pt.isel.keepmyplanet.ui.event.history

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import pt.isel.keepmyplanet.domain.common.Id
import pt.isel.keepmyplanet.ui.components.AppTopBar
import pt.isel.keepmyplanet.ui.components.EmptyState
import pt.isel.keepmyplanet.ui.components.ErrorState
import pt.isel.keepmyplanet.ui.components.FullScreenLoading
import pt.isel.keepmyplanet.ui.event.history.components.StatusHistoryItem

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
        topBar = { AppTopBar(title = "Status History", onNavigateBack = onNavigateBack) },
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
