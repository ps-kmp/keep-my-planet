package pt.isel.keepmyplanet.ui.event.list

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
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import pt.isel.keepmyplanet.ui.event.EventViewModel
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
    var tempLimit by remember { mutableStateOf(uiState.limit.toString()) }

    LaunchedEffect(uiState.limit) {
        tempLimit = uiState.limit.toString()
    }

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
        if (uiState.isLoading && uiState.events.isEmpty()) {
            CircularProgressIndicator(modifier = Modifier.padding(paddingValues).fillMaxSize())
        } else {
            Column(modifier = Modifier.padding(paddingValues).fillMaxSize()) {
                LazyColumn(
                    modifier = Modifier.weight(1f),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    items(uiState.events, key = { it.id.value.toInt() }) { event ->
                        EventItem(
                            event = event,
                            onClick = { onEventSelected(event) },
                        )
                    }
                }

                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        horizontalArrangement = Arrangement.SpaceBetween,
                        modifier = Modifier.fillMaxWidth(),
                    ) {
                        Button(
                            onClick = viewModel::loadPreviousPage,
                            enabled = uiState.canLoadPrevious,
                        ) {
                            Text("Previous")
                        }

                        Button(
                            onClick = viewModel::loadNextPage,
                            enabled = uiState.canLoadNext,
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
                            onValueChange = { value -> tempLimit = value.filter { it.isDigit() } },
                            keyboardOptions =
                                KeyboardOptions.Default.copy(
                                    keyboardType = KeyboardType.Number,
                                    imeAction = ImeAction.Done,
                                ),
                            keyboardActions =
                                KeyboardActions(
                                    onDone = {
                                        tempLimit.toIntOrNull()?.let { viewModel::changeLimit }
                                    },
                                ),
                            modifier = Modifier.width(100.dp),
                            singleLine = true,
                        )
                    }

                    Button(
                        onClick = onCreateEventClick,
                        modifier = Modifier.fillMaxWidth().padding(top = 16.dp),
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
