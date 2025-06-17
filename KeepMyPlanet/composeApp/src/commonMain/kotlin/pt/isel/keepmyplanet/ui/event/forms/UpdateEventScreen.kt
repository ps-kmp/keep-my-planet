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
import pt.isel.keepmyplanet.ui.components.FullScreenLoading
import pt.isel.keepmyplanet.ui.components.LoadingButton
import pt.isel.keepmyplanet.ui.event.forms.components.EventForm
import pt.isel.keepmyplanet.ui.event.forms.model.EventFormScreenEvent

@Suppress("ktlint:standard:function-naming")
@Composable
fun UpdateEventScreen(
    viewModel: EventFormViewModel,
    eventId: Id,
    onNavigateBack: () -> Unit,
) {
    val formUiState by viewModel.formUiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(eventId) {
        viewModel.prepareForEdit(eventId)
    }

    LaunchedEffect(viewModel.events) {
        viewModel.events.collectLatest { event ->
            when (event) {
                is EventFormScreenEvent.NavigateBack -> onNavigateBack()
                is EventFormScreenEvent.ShowSnackbar ->
                    snackbarHostState.showSnackbar(event.message, duration = SnackbarDuration.Short)

                is EventFormScreenEvent.EventUpdated -> {}

                else -> {}
            }
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = { AppTopBar(title = "Edit Event", onNavigateBack = onNavigateBack) },
    ) { paddingValues ->
        if (formUiState.isLoading) {
            FullScreenLoading(modifier = Modifier.padding(paddingValues))
        } else {
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
                    onZoneIdChanged = { },
                    showZoneIdField = false,
                )

                Spacer(modifier = Modifier.weight(1f))

                LoadingButton(
                    onClick = viewModel::submit,
                    modifier = Modifier.fillMaxWidth(),
                    isLoading = formUiState.isSubmitting,
                    enabled = !formUiState.isSubmitting,
                    text = "Confirm",
                )
            }
        }
    }
}
