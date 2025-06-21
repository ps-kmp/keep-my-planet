package pt.isel.keepmyplanet.ui.event.list

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.items
import androidx.compose.material.CircularProgressIndicator
import androidx.compose.material.FloatingActionButton
import androidx.compose.material.Icon
import androidx.compose.material.Scaffold
import androidx.compose.material.SnackbarDuration
import androidx.compose.material.SnackbarHost
import androidx.compose.material.SnackbarHostState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
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
import pt.isel.keepmyplanet.ui.event.list.components.SearchBarAndFilters
import pt.isel.keepmyplanet.ui.event.list.model.EventListItem
import pt.isel.keepmyplanet.ui.event.list.model.EventListScreenEvent

private const val PAGINATION_THRESHOLD = 3

@Composable
fun EventListScreen(
    viewModel: EventListViewModel,
    listState: LazyListState,
    onEventSelected: (event: EventListItem) -> Unit,
    onNavigateBack: () -> Unit,
    onCreateEventClick: () -> Unit,
) {
    val uiState by viewModel.listUiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(uiState.filter) {
        viewModel.refreshEvents()
    }

    LaunchedEffect(viewModel.events) {
        viewModel.events.collectLatest { event ->
            if (event is EventListScreenEvent.ShowSnackbar) {
                snackbarHostState.showSnackbar(
                    message = event.message,
                    duration = SnackbarDuration.Short,
                )
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
                lastVisibleItemIndex >= uiState.events.size - PAGINATION_THRESHOLD
            ) {
                viewModel.loadNextPage()
            }
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = { AppTopBar(title = "Events", onNavigateBack = onNavigateBack) },
        floatingActionButton = {
            FloatingActionButton(onClick = onCreateEventClick) {
                Icon(Icons.Default.Add, contentDescription = "Create Event")
            }
        },
    ) { paddingValues ->
        Column(modifier = Modifier.padding(paddingValues).fillMaxSize()) {
            SearchBarAndFilters(
                query = uiState.query,
                onQueryChange = viewModel::onSearchQueryChanged,
                activeFilter = uiState.filter,
                onFilterChange = viewModel::onFilterChanged,
                isLoading = uiState.isLoading,
            )

            Box(modifier = Modifier.fillMaxSize()) {
                if (uiState.isLoading && uiState.events.isEmpty() && uiState.error == null) {
                    FullScreenLoading()
                } else if (uiState.error != null) {
                    ErrorState(
                        message = uiState.error!!,
                        onRetry = { viewModel.refreshEvents() },
                    )
                } else if (uiState.events.isEmpty()) {
                    EmptyState(onActionClick = onCreateEventClick)
                } else {
                    LazyColumn(
                        state = listState,
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp),
                    ) {
                        items(uiState.events, key = { it.id.value.toString() }) { event ->
                            EventItem(event = event, onClick = { onEventSelected(event) })
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
}
