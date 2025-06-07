package pt.isel.keepmyplanet.ui.event.create

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.OutlinedTextField
import androidx.compose.material.Scaffold
import androidx.compose.material.Text
import androidx.compose.material.TopAppBar
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
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
        topBar = {
            TopAppBar(
                title = { Text("Create Event") },
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
        Column(
            modifier = Modifier.fillMaxSize().padding(paddingValues).padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            OutlinedTextField(
                value = formUiState.title,
                onValueChange = viewModel::onTitleChanged,
                label = { Text("Title") },
                modifier = Modifier.fillMaxWidth(),
                enabled = !formUiState.isSubmitting,
            )

            OutlinedTextField(
                value = formUiState.description,
                onValueChange = viewModel::onDescriptionChanged,
                label = { Text("Description") },
                modifier = Modifier.fillMaxWidth(),
                minLines = 3,
                enabled = !formUiState.isSubmitting,
            )

            OutlinedTextField(
                value = formUiState.startDate,
                onValueChange = viewModel::onStartDateChanged,
                label = { Text("Start Date and Time (YYYY-MM-DDTHH:MM:SS)") },
                modifier = Modifier.fillMaxWidth(),
                enabled = !formUiState.isSubmitting,
            )

            OutlinedTextField(
                value = formUiState.maxParticipants,
                onValueChange = viewModel::onMaxParticipantsChanged,
                label = { Text("Max Participants (Optional)") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier.fillMaxWidth(),
                enabled = !formUiState.isSubmitting,
            )

            OutlinedTextField(
                value = formUiState.zoneId,
                onValueChange = viewModel::onZoneIdChanged,
                label = { Text("Zone ID") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier.fillMaxWidth(),
                enabled = !formUiState.isSubmitting,
            )

            Spacer(modifier = Modifier.weight(1f))

            LoadingButton(
                onClick = viewModel::createEvent,
                enabled = formUiState.canSubmit,
                isLoading = formUiState.isSubmitting,
                text = "Create Event",
            )
        }
    }
}
