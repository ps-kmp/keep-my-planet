package pt.isel.keepmyplanet.ui.screens.event

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.Card
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

val sampleChatEvents =
    listOf(
        EventInfo(1U, "event1"),
        EventInfo(2U, "event2"),
        EventInfo(3U, "event3"),
    )

@Suppress("ktlint:standard:function-naming")
@Composable
fun EventListScreen(
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
        LazyColumn(
            modifier = Modifier.padding(paddingValues).fillMaxSize(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            items(sampleChatEvents, key = { it.id }) { event ->
                EventListItem(
                    event = event,
                    onClick = { onEventSelected(event) },
                )
            }
        }
    }
}

@Suppress("ktlint:standard:function-naming")
@Composable
private fun EventListItem(
    event: EventInfo,
    onClick: () -> Unit,
) {
    Card(
        modifier = Modifier.fillMaxWidth().clickable(onClick = onClick),
        elevation = 2.dp,
    ) {
        Text(
            text = event.name,
            modifier = Modifier.padding(16.dp),
            style = MaterialTheme.typography.body1,
        )
    }
}
