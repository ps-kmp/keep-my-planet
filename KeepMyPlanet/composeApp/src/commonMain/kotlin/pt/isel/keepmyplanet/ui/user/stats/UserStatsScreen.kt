package pt.isel.keepmyplanet.ui.user.stats

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.CircularProgressIndicator
import androidx.compose.material.Scaffold
import androidx.compose.material.SnackbarDuration
import androidx.compose.material.SnackbarHost
import androidx.compose.material.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.flow.collectLatest
import pt.isel.keepmyplanet.ui.components.AppTopBar
import pt.isel.keepmyplanet.ui.components.ErrorState
import pt.isel.keepmyplanet.ui.components.FullScreenLoading
import pt.isel.keepmyplanet.ui.event.list.components.EmptyState
import pt.isel.keepmyplanet.ui.event.list.components.EventItem
import pt.isel.keepmyplanet.ui.event.list.model.EventListItem
import pt.isel.keepmyplanet.ui.user.stats.model.UserStatsEvent

private const val PAGINATION_THRESHOLD = 3

@Composable
fun UserStatsScreen(
    viewModel: UserStatsViewModel,
    onEventSelected: (EventListItem) -> Unit,
    onNavigateBack: () -> Unit,
) {
    val uiState by viewModel.uiState.collectAsState()
    val listState = rememberLazyListState()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(viewModel.events) {
        viewModel.events.collectLatest { event ->
            when (event) {
                is UserStatsEvent.ShowSnackbar -> {
                    snackbarHostState.showSnackbar(
                        message = event.message,
                        duration = SnackbarDuration.Short,
                    )
                }
            }
        }
    }

    LaunchedEffect(listState) {
        snapshotFlow {
            listState.layoutInfo.visibleItemsInfo
                .lastOrNull()
                ?.index
        }.collect { lastVisibleItemIndex ->
            if (lastVisibleItemIndex != null &&
                lastVisibleItemIndex >= uiState.attendedEvents.size - PAGINATION_THRESHOLD
            ) {
                viewModel.loadNextPage()
            }
        }
    }

    Scaffold(
        topBar = { AppTopBar(title = "My Attended Events", onNavigateBack = onNavigateBack) },
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
    ) { padding ->
        Box(modifier = Modifier.fillMaxSize().padding(padding)) {
            if (uiState.isLoading) {
                FullScreenLoading()
            } else if (uiState.error != null) {
                ErrorState(
                    message = uiState.error!!,
                    onRetry = { viewModel.loadAttendedEvents(isRefresh = true) },
                )
            } else if (uiState.attendedEvents.isEmpty()) {
                EmptyState(message = "You have not attended any events.")
            } else {
                LazyColumn(
                    state = listState,
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    items(uiState.attendedEvents, key = { it.id.value.toString() }) { event ->
                        EventItem(
                            event = event,
                            onClick = { onEventSelected(event) },
                        )
                    }
                    if (uiState.isAddingMore) {
                        item {
                            Box(
                                modifier = Modifier.fillMaxWidth().padding(16.dp),
                                contentAlignment = Alignment.Center,
                            ) {
                                CircularProgressIndicator()
                            }
                        }
                    }
                }
            }
        }
    }
}
