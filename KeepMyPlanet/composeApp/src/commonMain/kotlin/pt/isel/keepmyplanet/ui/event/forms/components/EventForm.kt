package pt.isel.keepmyplanet.ui.event.forms.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import pt.isel.keepmyplanet.ui.components.DateTimePicker
import pt.isel.keepmyplanet.ui.components.FormField
import pt.isel.keepmyplanet.ui.event.forms.states.EventFormUiState

@Composable
fun EventForm(
    formUiState: EventFormUiState,
    onTitleChanged: (String) -> Unit,
    onDescriptionChanged: (String) -> Unit,
    onStartDateChanged: (String) -> Unit,
    onEndDateChanged: (String) -> Unit,
    onMaxParticipantsChanged: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        FormField(
            value = formUiState.title,
            onValueChange = onTitleChanged,
            label = "Title",
            errorText = formUiState.titleError,
            enabled = formUiState.actionState == EventFormUiState.ActionState.Idle,
            singleLine = true,
        )

        FormField(
            value = formUiState.description,
            onValueChange = onDescriptionChanged,
            label = "Description",
            minLines = 3,
            errorText = formUiState.descriptionError,
            enabled = formUiState.actionState == EventFormUiState.ActionState.Idle,
        )

        DateTimePicker(
            value = formUiState.startDate,
            onValueChange = onStartDateChanged,
            label = "Start Date and Time",
            errorText = formUiState.startDateError,
            enabled = formUiState.actionState == EventFormUiState.ActionState.Idle,
        )

        DateTimePicker(
            value = formUiState.endDate,
            onValueChange = onEndDateChanged,
            label = "End Date and Time (Optional)",
            errorText = formUiState.endDateError,
            enabled = formUiState.actionState == EventFormUiState.ActionState.Idle,
        )

        FormField(
            value = formUiState.maxParticipants,
            onValueChange = onMaxParticipantsChanged,
            label = "Max Participants (Optional)",
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            errorText = formUiState.maxParticipantsError,
            enabled = formUiState.actionState == EventFormUiState.ActionState.Idle,
            singleLine = true,
        )
    }
}
