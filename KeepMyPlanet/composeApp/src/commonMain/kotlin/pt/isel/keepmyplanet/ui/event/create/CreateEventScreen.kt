package pt.isel.keepmyplanet.ui.event.create

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.Card
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Scaffold
import androidx.compose.material.SnackbarDuration
import androidx.compose.material.SnackbarHost
import androidx.compose.material.SnackbarHostState
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.flow.collectLatest
import pt.isel.keepmyplanet.ui.components.AppTopBar
import pt.isel.keepmyplanet.ui.components.FormField
import pt.isel.keepmyplanet.ui.components.LoadingButton
import pt.isel.keepmyplanet.ui.event.EventViewModel
import pt.isel.keepmyplanet.ui.event.model.EventScreenEvent

@Suppress("ktlint:standard:function-naming")
@Composable
fun CreateEventScreen(
    viewModel: EventViewModel,
    zoneId: UInt,
    onEventCreated: (eventId: UInt) -> Unit,
    onNavigateBack: () -> Unit,
) {
    val formUiState by viewModel.formUiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(key1 = zoneId) {
        viewModel.resetFormState()
        viewModel.prepareFormForZone(zoneId)
    }

    LaunchedEffect(viewModel.events) {
        viewModel.events.collectLatest { event ->
            when (event) {
                is EventScreenEvent.EventCreated -> onEventCreated(event.eventId)
                is EventScreenEvent.ShowSnackbar ->
                    snackbarHostState.showSnackbar(event.message, duration = SnackbarDuration.Short)

                else -> {}
            }
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = { AppTopBar(title = "Create Event", onNavigateBack = onNavigateBack) },
    ) { paddingValues ->
        Column(
            modifier =
                Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(paddingValues)
                    .padding(16.dp),
        ) {
            Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(16.dp)) {
                Card(modifier = Modifier.fillMaxWidth(), elevation = 2.dp) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            "Zone ID: $zoneId",
                            style = MaterialTheme.typography.h6,
                        )
                        Text(
                            "This event will be associated with the selected zone.",
                            style = MaterialTheme.typography.body2,
                        )
                    }
                }

                FormField(
                    value = formUiState.title,
                    onValueChange = viewModel::onTitleChanged,
                    label = "Event Title",
                    errorText = formUiState.titleError,
                    enabled = !formUiState.isSubmitting,
                    singleLine = true,
                )

                FormField(
                    value = formUiState.description,
                    onValueChange = viewModel::onDescriptionChanged,
                    label = "Description",
                    minLines = 3,
                    errorText = formUiState.descriptionError,
                    enabled = !formUiState.isSubmitting,
                )

                FormField(
                    value = formUiState.startDate,
                    onValueChange = viewModel::onStartDateChanged,
                    label = "Start Date and Time (YYYY-MM-DDTHH:MM)",
                    errorText = formUiState.startDateError,
                    enabled = !formUiState.isSubmitting,
                    singleLine = true,
                )

                FormField(
                    value = formUiState.maxParticipants,
                    onValueChange = viewModel::onMaxParticipantsChanged,
                    label = "Max Participants (Optional)",
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    errorText = formUiState.maxParticipantsError,
                    enabled = !formUiState.isSubmitting,
                    singleLine = true,
                )
            }

            Spacer(modifier = Modifier.padding(8.dp))

            LoadingButton(
                onClick = viewModel::createEvent,
                enabled = !formUiState.isSubmitting,
                isLoading = formUiState.isSubmitting,
                text = "Create Event",
                modifier = Modifier.fillMaxWidth(),
            )
        }
    }
}
