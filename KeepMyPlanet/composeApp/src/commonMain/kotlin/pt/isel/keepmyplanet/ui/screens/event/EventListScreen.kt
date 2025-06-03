package pt.isel.keepmyplanet.ui.screens.event

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.Button
import androidx.compose.material.CircularProgressIndicator
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Scaffold
import androidx.compose.material.Text
import androidx.compose.material.TextField
import androidx.compose.material.TopAppBar
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import pt.isel.keepmyplanet.data.model.EventInfo
import pt.isel.keepmyplanet.ui.screens.event.components.EventItem

@Suppress("ktlint:standard:function-naming")
@Composable
fun EventListScreen(
    uiState: EventListUiState,
    onEventSelected: (event: EventInfo) -> Unit,
    onNavigateBack: () -> Unit,
    onCreateEventClick: () -> Unit,
    onLoadEvents: (limit: Int, offset: Int) -> Unit,
) {
    var limit by remember { mutableStateOf(20) }
    var offset by remember { mutableStateOf(0) }
    var tempLimit by remember { mutableStateOf(limit.toString()) }

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
        if (uiState.isLoading) {
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
                    items(uiState.events, key = { it.id }) { event ->
                        EventItem(
                            event = event,
                            onClick = { onEventSelected(event) },
                        )
                    }
                }

                Column(
                    modifier = Modifier.padding(16.dp),
                ) {
                    Row(
                        horizontalArrangement = Arrangement.SpaceBetween,
                        modifier = Modifier.fillMaxWidth(),
                    ) {
                        Button(
                            onClick = {
                                offset = (offset - limit).coerceAtLeast(0)
                                onLoadEvents(limit, offset)
                            },
                            enabled = offset > 0,
                        ) {
                            Text("Previous")
                        }

                        Button(
                            onClick = {
                                offset += limit
                                onLoadEvents(limit, offset)
                            },
                            enabled = uiState.events.size == limit,
                        ) {
                            Text("Next")
                        }
                    }

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(top = 8.dp),
                    ) {
                        Text("Limit: ", modifier = Modifier.padding(end = 8.dp))
                        TextField(
                            value = tempLimit,
                            onValueChange = { newValue ->
                                tempLimit = newValue
                            },
                            keyboardOptions =
                                KeyboardOptions.Default.copy(
                                    imeAction = ImeAction.Done,
                                ),
                            keyboardActions =
                                KeyboardActions(
                                    onDone = {
                                        tempLimit.toIntOrNull()?.let {
                                            limit = it.coerceAtLeast(1)
                                            offset = 0 // Restart offset when limit changes
                                            onLoadEvents(limit, offset)
                                        }
                                    },
                                ),
                            modifier = Modifier.width(100.dp),
                            singleLine = true,
                        )
                    }

                    Button(
                        onClick = onCreateEventClick,
                        modifier =
                            Modifier
                                .fillMaxWidth()
                                .padding(top = 16.dp),
                    ) {
                        Text("Create Event")
                    }

                    uiState.error?.let {
                        Text(
                            text = it,
                            color = MaterialTheme.colors.error,
                            modifier = Modifier.padding(top = 8.dp),
                        )
                    }
                }
            }
        }
    }
}
