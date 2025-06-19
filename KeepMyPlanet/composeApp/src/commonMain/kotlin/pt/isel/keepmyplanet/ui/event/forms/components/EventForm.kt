package pt.isel.keepmyplanet.ui.event.forms.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import pt.isel.keepmyplanet.ui.components.FormField
import pt.isel.keepmyplanet.ui.event.forms.model.EventFormUiState

@Composable
fun EventForm(
    formUiState: EventFormUiState,
    onTitleChanged: (String) -> Unit,
    onDescriptionChanged: (String) -> Unit,
    onStartDateChanged: (String) -> Unit,
    onMaxParticipantsChanged: (String) -> Unit,
    onZoneIdChanged: (String) -> Unit,
    showZoneIdField: Boolean,
    isZoneIdEditable: Boolean = true,
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
            enabled = !formUiState.isSubmitting,
            singleLine = true,
        )

        FormField(
            value = formUiState.description,
            onValueChange = onDescriptionChanged,
            label = "Description",
            minLines = 3,
            errorText = formUiState.descriptionError,
            enabled = !formUiState.isSubmitting,
        )

        FormField(
            value = formUiState.startDate,
            onValueChange = onStartDateChanged,
            label = "Start Date and Time (YYYY-MM-DDTHH:MM:SS)",
            errorText = formUiState.startDateError,
            enabled = !formUiState.isSubmitting,
            singleLine = true,
        )

        FormField(
            value = formUiState.maxParticipants,
            onValueChange = onMaxParticipantsChanged,
            label = "Max Participants (Optional)",
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            errorText = formUiState.maxParticipantsError,
            enabled = !formUiState.isSubmitting,
            singleLine = true,
        )

        if (showZoneIdField) {
            FormField(
                value = formUiState.zoneId,
                onValueChange = onZoneIdChanged,
                label = "Zone ID",
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                enabled = !formUiState.isSubmitting && isZoneIdEditable,
                singleLine = true,
                errorText = formUiState.zoneIdError,
            )
        }
    }
}
