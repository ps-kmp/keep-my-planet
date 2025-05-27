package pt.isel.keepmyplanet.ui.screens.event

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.Button
import androidx.compose.material.CircularProgressIndicator
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Scaffold
import androidx.compose.material.Text
import androidx.compose.material.TopAppBar
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import pt.isel.keepmyplanet.data.model.EventInfo
import pt.isel.keepmyplanet.ui.screens.event.components.EventItem

@Suppress("ktlint:standard:function-naming")
@Composable
fun EventListScreen(
    events: List<EventInfo>,
    isLoading: Boolean,
    error: String?,
    onEventSelected: (event: EventInfo) -> Unit,
    onNavigateBack: () -> Unit,
    onCreateEventClick: () -> Unit,
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Events") },
                elevation = 4.dp,
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                        )
                    }
                },
            )
        },
    ) { paddingValues ->
        if (isLoading) {
            CircularProgressIndicator(
                modifier =
                    Modifier
                        .padding(paddingValues)
                        .fillMaxSize(),
            )
        } else {
            Column(
                modifier =
                    Modifier
                        .padding(paddingValues)
                        .fillMaxSize(),
            ) {
                LazyColumn(
                    modifier = Modifier.weight(1f),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    items(events, key = { it.id }) { event ->
                        EventItem(
                            event = event,
                            onClick = { onEventSelected(event) },
                        )
                    }
                }

                Button(
                    onClick = onCreateEventClick,
                    modifier =
                        Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                ) {
                    Text("Create Event")
                }

                error?.let {
                    Text(
                        text = it,
                        color = MaterialTheme.colors.error,
                        modifier = Modifier.padding(16.dp),
                    )
                }
            }
        }
    }
}
