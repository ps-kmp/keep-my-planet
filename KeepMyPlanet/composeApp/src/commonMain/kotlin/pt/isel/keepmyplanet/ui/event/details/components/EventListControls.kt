package pt.isel.keepmyplanet.ui.event.details.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.Button
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.material.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import pt.isel.keepmyplanet.ui.event.model.EventListUiState

@Suppress("ktlint:standard:function-naming")
@Composable
fun EventListControls(
    uiState: EventListUiState,
    onLoadPreviousPage: () -> Unit,
    onLoadNextPage: () -> Unit,
    onChangeLimit: (Int) -> Unit,
    onCreateEventClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    var tempLimit by remember(uiState.limit) { mutableStateOf(uiState.limit.toString()) }

    Column(modifier = modifier.padding(16.dp)) {
        Row(
            horizontalArrangement = Arrangement.SpaceBetween,
            modifier = Modifier.fillMaxWidth(),
        ) {
            Button(
                onClick = onLoadPreviousPage,
                enabled = uiState.canLoadPrevious,
            ) {
                Text("Previous")
            }

            Button(
                onClick = onLoadNextPage,
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
                            tempLimit.toIntOrNull()?.let(onChangeLimit)
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
