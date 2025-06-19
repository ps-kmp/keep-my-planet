package pt.isel.keepmyplanet.ui.user.stats

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import pt.isel.keepmyplanet.domain.event.Event
import pt.isel.keepmyplanet.ui.components.AppTopBar
import pt.isel.keepmyplanet.ui.components.ErrorState
import pt.isel.keepmyplanet.ui.components.FullScreenLoading
import pt.isel.keepmyplanet.ui.event.list.components.EventItem

@Composable
fun UserStatsScreen(
    viewModel: UserStatsViewModel,
    onEventSelected: (Event) -> Unit,
    onNavigateBack: () -> Unit,
) {
    val uiState by viewModel.uiState.collectAsState()

    Scaffold(
        topBar = { AppTopBar(title = "My Attended Events", onNavigateBack = onNavigateBack) },
    ) { padding ->
        if (uiState.isLoading) {
            FullScreenLoading()
        } else if (uiState.error != null) {
            ErrorState(message = uiState.error!!, onRetry = viewModel::loadAttendedEvents)
        } else {
            LazyColumn(modifier = Modifier.fillMaxSize().padding(padding)) {
                items(uiState.attendedEvents) { event ->
                    // RESOLVER ISTO
                    EventItem(event = event.toListItem(), onClick = { onEventSelected(event) })
                }
            }
        }
    }
}
