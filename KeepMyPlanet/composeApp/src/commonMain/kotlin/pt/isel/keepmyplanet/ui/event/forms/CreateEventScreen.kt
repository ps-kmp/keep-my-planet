package pt.isel.keepmyplanet.ui.event.forms

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.flow.collectLatest
import pt.isel.keepmyplanet.domain.common.Id
import pt.isel.keepmyplanet.navigation.rememberSavableScrollState
import pt.isel.keepmyplanet.ui.components.AppTopBar
import pt.isel.keepmyplanet.ui.components.LoadingButton
import pt.isel.keepmyplanet.ui.event.forms.components.EventForm
import pt.isel.keepmyplanet.ui.event.forms.states.EventFormEvent
import pt.isel.keepmyplanet.ui.event.forms.states.EventFormUiState

@Composable
fun CreateEventScreen(
    viewModel: EventFormViewModel,
    onNavigateToHome: () -> Unit,
    zoneId: Id?,
    onEventCreated: (eventId: Id) -> Unit,
    onNavigateBack: () -> Unit,
    routeKey: String,
) {
    val uiState by viewModel.uiState.collectAsState()
    val scrollState = rememberSavableScrollState(key = routeKey)
    val snackbarHostState = remember { SnackbarHostState() }
    val isActionInProgress = uiState.actionState is EventFormUiState.ActionState.Submitting

    LaunchedEffect(Unit) {
        viewModel.prepareForCreate(zoneId)
    }

    LaunchedEffect(viewModel.events) {
        viewModel.events.collectLatest { event ->
            when (event) {
                is EventFormEvent.EventCreated -> onEventCreated(event.eventId)
                is EventFormEvent.ShowSnackbar ->
                    snackbarHostState.showSnackbar(event.message, duration = SnackbarDuration.Short)

                is EventFormEvent.NavigateBack -> { /* Not used in create screen */ }
            }
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            AppTopBar(
                title = "Create Event",
                onNavigateBack = onNavigateBack,
                onNavigateToHome = onNavigateToHome,
            )
        },
    ) { paddingValues ->
        Column(modifier = Modifier.fillMaxSize().padding(paddingValues).padding(16.dp)) {
            Column(
                modifier = Modifier.weight(1f).verticalScroll(scrollState),
                verticalArrangement = Arrangement.spacedBy(16.dp),
            ) {
                EventForm(
                    formUiState = uiState,
                    onTitleChanged = viewModel::onTitleChanged,
                    onDescriptionChanged = viewModel::onDescriptionChanged,
                    onStartDateChanged = viewModel::onStartDateChanged,
                    onEndDateChanged = viewModel::onEndDateChanged,
                    onEndDateCleared = viewModel::onEndDateCleared,
                    onMaxParticipantsChanged = viewModel::onMaxParticipantsChanged,
                )
            }
            Spacer(modifier = Modifier.weight(0.1f, fill = false))

            LoadingButton(
                onClick = viewModel::submit,
                enabled = !isActionInProgress,
                isLoading = isActionInProgress,
                modifier = Modifier.fillMaxWidth(),
            ) {
                Text("Create Event")
            }
        }
    }
}
