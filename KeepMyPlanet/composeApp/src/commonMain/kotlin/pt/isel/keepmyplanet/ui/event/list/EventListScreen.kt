
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
import androidx.compose.material.icons.filled.AddLocation
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
import pt.isel.keepmyplanet.ui.components.FullScreenLoading
import pt.isel.keepmyplanet.ui.event.EventViewModel
import pt.isel.keepmyplanet.ui.event.components.ErrorState
import pt.isel.keepmyplanet.ui.event.list.components.EmptyState
import pt.isel.keepmyplanet.ui.event.list.components.EventItem
import pt.isel.keepmyplanet.ui.event.list.components.SearchBarAndFilters
import pt.isel.keepmyplanet.ui.event.model.EventListItem
import pt.isel.keepmyplanet.ui.event.model.EventScreenEvent

@Suppress("ktlint:standard:function-naming")
@Composable
fun EventListScreen(
    viewModel: EventViewModel,
    listState: LazyListState,
    onEventSelected: (event: EventListItem) -> Unit,
    onNavigateBack: () -> Unit,
    onNavigateToMap: () -> Unit,
) {
    val uiState by viewModel.listUiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(viewModel.events) {
        viewModel.events.collectLatest { event ->
            if (event is EventScreenEvent.ShowSnackbar) {
                snackbarHostState.showSnackbar(event.message, duration = SnackbarDuration.Short)
            }
        }
    }

    LaunchedEffect(listState) {
        snapshotFlow {
            listState.layoutInfo.visibleItemsInfo
                .lastOrNull()
                ?.index
        }.collect { lastVisibleItemIndex ->
            if (lastVisibleItemIndex != null && lastVisibleItemIndex >= uiState.events.size - 3) {
                viewModel.loadNextPage()
            }
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = { AppTopBar(title = "Events", onNavigateBack = onNavigateBack) },
        floatingActionButton = {
            FloatingActionButton(onClick = onNavigateToMap) {
                Icon(Icons.Default.AddLocation, contentDescription = "Create Event by selecting a Zone")
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

            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center,
            ) {
                if (uiState.isLoading && uiState.events.isEmpty()) {
                    FullScreenLoading()
                } else if (uiState.error != null) {
                    ErrorState(message = uiState.error!!, onRetry = viewModel::refreshEvents)
                } else if (uiState.events.isEmpty()) {
                    EmptyState(onActionClick = onNavigateToMap)
                } else {
                    LazyColumn(
                        state = listState,
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp),
                    ) {
                        items(uiState.events, key = { it.id.value.toInt() }) { event ->
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
