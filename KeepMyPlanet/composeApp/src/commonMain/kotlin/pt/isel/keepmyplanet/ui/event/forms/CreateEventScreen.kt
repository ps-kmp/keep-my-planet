package pt.isel.keepmyplanet.ui.event.forms

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.Scaffold
import androidx.compose.material.SnackbarDuration
import androidx.compose.material.SnackbarHost
import androidx.compose.material.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.flow.collectLatest
import pt.isel.keepmyplanet.domain.common.Id
import pt.isel.keepmyplanet.ui.components.AppTopBar
import pt.isel.keepmyplanet.ui.components.LoadingButton
import pt.isel.keepmyplanet.ui.event.forms.components.EventForm
import pt.isel.keepmyplanet.ui.event.forms.model.EventFormScreenEvent

@Composable
fun CreateEventScreen(
    viewModel: EventFormViewModel,
    zoneId: Id?,
    onEventCreated: (eventId: Id) -> Unit,
    onNavigateBack: () -> Unit,
) {
    val formUiState by viewModel.formUiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(Unit) {
        viewModel.prepareForCreate(zoneId)
    }

    LaunchedEffect(viewModel.events) {
        viewModel.events.collectLatest { event ->
            when (event) {
                is EventFormScreenEvent.EventCreated -> onEventCreated(event.eventId)
                is EventFormScreenEvent.ShowSnackbar ->
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
            modifier = Modifier.fillMaxSize().padding(paddingValues).padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            EventForm(
                formUiState = formUiState,
                onTitleChanged = viewModel::onTitleChanged,
                onDescriptionChanged = viewModel::onDescriptionChanged,
                onStartDateChanged = viewModel::onStartDateChanged,
                onMaxParticipantsChanged = viewModel::onMaxParticipantsChanged,
                onZoneIdChanged = viewModel::onZoneIdChanged,
                showZoneIdField = true,
                isZoneIdEditable = (zoneId == null),
            )

            Spacer(modifier = Modifier.weight(1f))

            LoadingButton(
                onClick = viewModel::submit,
                enabled = !formUiState.isSubmitting,
                isLoading = formUiState.isSubmitting,
                text = "Create Event",
                modifier = Modifier.fillMaxWidth(),
            )
        }
    }
}
