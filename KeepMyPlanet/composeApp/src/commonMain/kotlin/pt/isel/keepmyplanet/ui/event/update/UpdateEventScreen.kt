package pt.isel.keepmyplanet.ui.event.update

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.Button
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.flow.collectLatest
import pt.isel.keepmyplanet.domain.common.Id
import pt.isel.keepmyplanet.ui.components.AppTopBar
import pt.isel.keepmyplanet.ui.components.FullScreenLoading
import pt.isel.keepmyplanet.ui.components.LoadingButton
import pt.isel.keepmyplanet.ui.event.EventViewModel
import pt.isel.keepmyplanet.ui.event.components.ErrorState
import pt.isel.keepmyplanet.ui.event.components.EventForm
import pt.isel.keepmyplanet.ui.event.model.EventScreenEvent

@Suppress("ktlint:standard:function-naming")
@Composable
fun UpdateEventScreen(
    viewModel: EventViewModel,
    eventId: UInt,
    userId: UInt,
    onNavigateBack: () -> Unit,
) {
    val detailsState by viewModel.detailsUiState.collectAsState()
    val formUiState by viewModel.formUiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    val currentUserId = remember { Id(userId) }

    LaunchedEffect(eventId) {
        viewModel.loadEventDetails(eventId)
    }

    LaunchedEffect(detailsState.event) {
        if (detailsState.event?.id?.value == eventId) {
            viewModel.prepareFormForEdit()
        }
    }

    LaunchedEffect(viewModel.events) {
        viewModel.events.collectLatest { event ->
            when (event) {
                is EventScreenEvent.NavigateBack -> onNavigateBack()
                is EventScreenEvent.ShowSnackbar ->
                    snackbarHostState.showSnackbar(event.message, duration = SnackbarDuration.Short)

                else -> {}
            }
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = { AppTopBar(title = "Edit Event", onNavigateBack = onNavigateBack) },
    ) { paddingValues ->
        Box(
            modifier = Modifier.fillMaxSize().padding(paddingValues),
            contentAlignment = Alignment.Center,
        ) {
            when {
                detailsState.isLoading -> {
                    FullScreenLoading()
                }

                detailsState.error != null -> {
                    ErrorState(
                        message = detailsState.error!!,
                        onRetry = { viewModel.loadEventDetails(eventId) },
                    )
                }

                detailsState.event != null -> {
                    val canEdit = detailsState.canUserEdit(currentUserId)
                    Column(
                        modifier =
                            Modifier
                                .fillMaxSize()
                                .verticalScroll(rememberScrollState())
                                .padding(16.dp),
                    ) {
                        Column(
                            modifier = Modifier.weight(1f),
                            verticalArrangement = Arrangement.spacedBy(16.dp),
                        ) {
                            Card(elevation = 2.dp) {
                                Column(modifier = Modifier.fillMaxWidth().padding(16.dp)) {
                                    Text(
                                        "Editing Event: ${detailsState.event?.title?.value}",
                                        style = MaterialTheme.typography.h6,
                                    )
                                    Text(
                                        "Zone ID: ${detailsState.event?.zoneId?.value}",
                                        style = MaterialTheme.typography.body2,
                                    )
                                }
                            }

                            if (canEdit) {
                                EventForm(
                                    formUiState = formUiState,
                                    onTitleChanged = viewModel::onTitleChanged,
                                    onDescriptionChanged = viewModel::onDescriptionChanged,
                                    onStartDateChanged = viewModel::onStartDateChanged,
                                    onMaxParticipantsChanged = viewModel::onMaxParticipantsChanged,
                                    onZoneIdChanged = { },
                                    showZoneIdField = false,
                                )
                            } else {
                                Text(
                                    text = "This event can no longer be edited because it's not in a 'PLANNED' state.",
                                    modifier = Modifier.fillMaxWidth().padding(16.dp),
                                    textAlign = TextAlign.Center,
                                    color = MaterialTheme.colors.onSurface.copy(alpha = 0.7f),
                                )
                            }
                        }

                        if (canEdit) {
                            LoadingButton(
                                onClick = { viewModel.updateEvent(eventId) },
                                modifier = Modifier.fillMaxWidth().padding(top = 16.dp),
                                isLoading = formUiState.isSubmitting,
                                enabled = !formUiState.isSubmitting,
                                text = "Save Changes",
                            )
                        } else {
                            Button(
                                onClick = onNavigateBack,
                                modifier = Modifier.fillMaxWidth().padding(top = 16.dp),
                            ) {
                                Text("Go Back")
                            }
                        }
                    }
                }
            }
        }
    }
}
