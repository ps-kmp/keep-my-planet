package pt.isel.keepmyplanet.ui.screens.event

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
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

/*val sampleChatEvents =
    listOf(
        EventInfo(1U, "event1"),
        EventInfo(2U, "event2"),
        EventInfo(3U, "event3"),
    )*/

@Suppress("ktlint:standard:function-naming")
@Composable
fun EventListScreen(
    events: List<EventInfo>,
    isLoading: Boolean,
    error: String?,
    onEventSelected: (event: EventInfo) -> Unit,
    onNavigateBack: () -> Unit,
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Chat Events") },
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
                modifier = Modifier.padding(paddingValues).fillMaxSize(),
            )
        } else {
            LazyColumn(
                modifier = Modifier.padding(paddingValues).fillMaxSize(),
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
