package pt.isel.keepmyplanet.ui.event.forms

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
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
import pt.isel.keepmyplanet.ui.components.FullScreenLoading
import pt.isel.keepmyplanet.ui.components.LoadingButton
import pt.isel.keepmyplanet.ui.event.forms.components.EventForm
import pt.isel.keepmyplanet.ui.event.forms.states.EventFormEvent
import pt.isel.keepmyplanet.ui.event.forms.states.EventFormUiState

@Composable
fun UpdateEventScreen(
    viewModel: EventFormViewModel,
    eventId: Id,
    onNavigateBack: () -> Unit,
) {
    val uiState by viewModel.uiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    val isActionInProgress = uiState.actionState is EventFormUiState.ActionState.Submitting

    LaunchedEffect(eventId) {
        viewModel.prepareForEdit(eventId)
    }

    LaunchedEffect(viewModel.events) {
        viewModel.events.collectLatest { event ->
            when (event) {
                is EventFormEvent.NavigateBack -> onNavigateBack()
                is EventFormEvent.ShowSnackbar ->
                    snackbarHostState.showSnackbar(event.message, duration = SnackbarDuration.Short)
                is EventFormEvent.EventCreated -> { /* Not used in update screen */ }
            }
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = { AppTopBar(title = "Edit Event", onNavigateBack = onNavigateBack) },
    ) { paddingValues ->
        if (uiState.isLoading) {
            FullScreenLoading(modifier = Modifier.padding(paddingValues))
        } else {
            Column(
                modifier = Modifier.fillMaxSize().padding(paddingValues).padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
            ) {
                EventForm(
                    formUiState = uiState,
                    onTitleChanged = viewModel::onTitleChanged,
                    onDescriptionChanged = viewModel::onDescriptionChanged,
                    onStartDateChanged = viewModel::onStartDateChanged,
                    onMaxParticipantsChanged = viewModel::onMaxParticipantsChanged,
                    onZoneIdChanged = { },
                    showZoneIdField = false,
                )

                Spacer(modifier = Modifier.weight(1f))

                LoadingButton(
                    onClick = viewModel::submit,
                    modifier = Modifier.fillMaxWidth(),
                    isLoading = isActionInProgress,
                    enabled = !isActionInProgress,
                    text = "Confirm",
                )
            }
        }
    }
}
