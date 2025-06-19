package pt.isel.keepmyplanet.ui.event.details

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Scaffold
import androidx.compose.material.SnackbarDuration
import androidx.compose.material.SnackbarHost
import androidx.compose.material.SnackbarHostState
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Flag
import androidx.compose.material.icons.filled.People
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.flow.collectLatest
import pt.isel.keepmyplanet.domain.common.Id
import pt.isel.keepmyplanet.ui.chat.model.ChatInfo
import pt.isel.keepmyplanet.ui.components.AppTopBar
import pt.isel.keepmyplanet.ui.components.DetailCard
import pt.isel.keepmyplanet.ui.components.ErrorState
import pt.isel.keepmyplanet.ui.components.FullScreenLoading
import pt.isel.keepmyplanet.ui.components.InfoRow
import pt.isel.keepmyplanet.ui.components.ManageAttendanceButton
import pt.isel.keepmyplanet.ui.components.QrCodeIconButton
import pt.isel.keepmyplanet.ui.components.toFormattedString
import pt.isel.keepmyplanet.ui.event.details.components.EventActions
import pt.isel.keepmyplanet.ui.event.details.components.ParticipantRow
import pt.isel.keepmyplanet.ui.event.details.model.EventDetailsScreenEvent

@Composable
fun EventDetailsScreen(
    viewModel: EventDetailsViewModel,
    eventId: Id,
    onNavigateToChat: (ChatInfo) -> Unit,
    onNavigateToEditEvent: (Id) -> Unit,
    onNavigateToManageAttendance: (Id) -> Unit,
    onNavigateToMyQrCode: (Id) -> Unit,
    onNavigateBack: () -> Unit,
) {
    val uiState by viewModel.detailsUiState.collectAsState()
    val event = uiState.event
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(viewModel.events) {
        viewModel.events.collectLatest { event ->
            when (event) {
                is EventDetailsScreenEvent.ShowSnackbar -> {
                    snackbarHostState.showSnackbar(
                        message = event.message,
                        duration = SnackbarDuration.Short,
                    )
                }

                is EventDetailsScreenEvent.EventDeleted -> {
                    onNavigateBack()
                }

                is EventDetailsScreenEvent.NavigateTo -> {
                    when (val dest = event.destination) {
                        is EventDetailsViewModel.QrNavigation.ToScanner -> onNavigateToManageAttendance(dest.eventId)
                        is EventDetailsViewModel.QrNavigation.ToMyCode -> onNavigateToMyQrCode(dest.userId)
                    }
                }

                else -> {}
            }
        }
    }

    LaunchedEffect(eventId) {
        viewModel.loadEventDetails(eventId)
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            AppTopBar(
                title = event?.title?.value ?: "Event Details",
                onNavigateBack = onNavigateBack,
                actions = {
                    if (uiState.canUseQrFeature()) {
                        QrCodeIconButton(
                            onClick = viewModel::onQrCodeIconClicked,
                            contentDescription = "Open QR Code Feature"
                        )
                    }
                }
            )
        },
    ) { paddingValues ->
        Box(modifier = Modifier.fillMaxSize().padding(paddingValues)) {
            if (uiState.isLoading) {
                FullScreenLoading()
            } else if (uiState.event == null || uiState.error != null) {
                ErrorState(message = uiState.error ?: "Failed to load event.") {
                    viewModel.loadEventDetails(eventId)
                }
            } else if (event != null) {
                Column(
                    modifier =
                        Modifier
                            .fillMaxSize()
                            .verticalScroll(rememberScrollState())
                            .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                ) {
                    Text(
                        text = event.title.value,
                        style = MaterialTheme.typography.h5,
                    )

                    DetailCard(title = "Description") {
                        Text(
                            text = event.description.value,
                            style = MaterialTheme.typography.body1,
                        )
                    }

                    DetailCard(title = "Information") {
                        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                            InfoRow(
                                icon = Icons.Default.Schedule,
                                text = "Starts: ${event.period.start.toFormattedString()}",
                            )
                            event.period.end?.let {
                                InfoRow(
                                    icon = Icons.Default.Schedule,
                                    text = "Ends: ${it.toFormattedString()}",
                                )
                            }
                            InfoRow(icon = Icons.Default.Flag, text = "Status: ${event.status}")
                            event.maxParticipants?.let {
                                InfoRow(
                                    icon = Icons.Default.People,
                                    text = "Participants: ${event.participantsIds.size}/$it",
                                )
                            }
                        }
                    }
                    if (uiState.participants.isNotEmpty()) {
                        DetailCard("Participants (${uiState.participants.size})") {
                            Column(modifier = Modifier.fillMaxWidth()) {
                                uiState.participants.forEach { participant ->
                                    ParticipantRow(participant)
                                }
                            }
                        }
                    }

                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Text(
                            text = "Created: ${event.createdAt.toFormattedString()}",
                            style = MaterialTheme.typography.caption,
                        )
                        Text(
                            text = "Last update: ${event.updatedAt.toFormattedString()}",
                            style = MaterialTheme.typography.caption,
                        )
                    }

                    Spacer(modifier = Modifier.weight(1f))

                    if (uiState.canManageAttendance()) {
                        ManageAttendanceButton {
                            onNavigateToManageAttendance(event.id)
                        }
                    }

                    Spacer(modifier = Modifier.weight(1f))

                    EventActions(
                        uiState = uiState,
                        onJoinEvent = viewModel::joinEvent,
                        onLeaveEvent = viewModel::leaveEvent,
                        onNavigateToChat = onNavigateToChat,
                        onNavigateToEditEvent = { onNavigateToEditEvent(event.id) },
                        onCancelEvent = viewModel::cancelEvent,
                        onCompleteEvent = viewModel::completeEvent,
                        onDeleteEvent = viewModel::deleteEvent,
                    )
                }
            }
        }
    }
}
