package pt.isel.keepmyplanet.ui.event.list

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import pt.isel.keepmyplanet.ui.components.AppTopBar
import pt.isel.keepmyplanet.ui.components.FullScreenLoading
import pt.isel.keepmyplanet.ui.event.EventViewModel
import pt.isel.keepmyplanet.ui.event.details.components.EventListControls
import pt.isel.keepmyplanet.ui.event.list.components.EventItem
import pt.isel.keepmyplanet.ui.event.model.EventListItem

@Suppress("ktlint:standard:function-naming")
@Composable
fun EventListScreen(
    viewModel: EventViewModel,
    onEventSelected: (event: EventListItem) -> Unit,
    onNavigateBack: () -> Unit,
    onCreateEventClick: () -> Unit,
) {
    val uiState by viewModel.listUiState.collectAsState()

    Scaffold(
        topBar = { AppTopBar(title = "Events", onNavigateBack = onNavigateBack) },
    ) { paddingValues ->
        if (uiState.isLoading && uiState.events.isEmpty()) {
            FullScreenLoading(modifier = Modifier.padding(paddingValues))
        } else {
            Column(modifier = Modifier.padding(paddingValues).fillMaxSize()) {
                LazyColumn(
                    modifier = Modifier.weight(1f),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    items(uiState.events, key = { it.id.value.toInt() }) { event ->
                        EventItem(event = event, onClick = { onEventSelected(event) })
                    }
                }

                EventListControls(
                    uiState = uiState,
                    onLoadPreviousPage = viewModel::loadPreviousPage,
                    onLoadNextPage = viewModel::loadNextPage,
                    onChangeLimit = viewModel::changeLimit,
                    onCreateEventClick = onCreateEventClick,
                )
            }
        }
    }
}
