package pt.isel.keepmyplanet.ui.event.create

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import pt.isel.keepmyplanet.ui.components.AppTopBar
import pt.isel.keepmyplanet.ui.components.FormField
import pt.isel.keepmyplanet.ui.components.LoadingButton
import pt.isel.keepmyplanet.ui.event.EventViewModel

@Suppress("ktlint:standard:function-naming")
@Composable
fun CreateEventScreen(
    viewModel: EventViewModel,
    onNavigateBack: () -> Unit,
) {
    val formUiState by viewModel.formUiState.collectAsState()

    Scaffold(
        topBar = { AppTopBar(title = "Create Event", onNavigateBack = onNavigateBack) },
    ) { paddingValues ->
        Column(
            modifier = Modifier.fillMaxSize().padding(paddingValues).padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            FormField(
                value = formUiState.title,
                onValueChange = viewModel::onTitleChanged,
                label = "Title",
                enabled = !formUiState.isSubmitting,
                singleLine = true,
            )

            FormField(
                value = formUiState.description,
                onValueChange = viewModel::onDescriptionChanged,
                label = "Description",
                minLines = 3,
                enabled = !formUiState.isSubmitting,
            )

            FormField(
                value = formUiState.startDate,
                onValueChange = viewModel::onStartDateChanged,
                label = "Start Date and Time (YYYY-MM-DDTHH:MM:SS)",
                enabled = !formUiState.isSubmitting,
                singleLine = true,
            )

            FormField(
                value = formUiState.maxParticipants,
                onValueChange = viewModel::onMaxParticipantsChanged,
                label = "Max Participants (Optional)",
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                enabled = !formUiState.isSubmitting,
                singleLine = true,
            )

            FormField(
                value = formUiState.zoneId,
                onValueChange = viewModel::onZoneIdChanged,
                label = "Zone ID",
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                enabled = !formUiState.isSubmitting,
                singleLine = true,
            )

            Spacer(modifier = Modifier.weight(1f))

            LoadingButton(
                onClick = viewModel::createEvent,
                enabled = formUiState.canSubmit,
                isLoading = formUiState.isSubmitting,
                text = "Create Event",
                modifier = Modifier.fillMaxWidth(),
            )
        }
    }
}
