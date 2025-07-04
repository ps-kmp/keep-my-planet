package pt.isel.keepmyplanet.ui.event.details

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Flag
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.flow.collectLatest
import pt.isel.keepmyplanet.domain.common.Id
import pt.isel.keepmyplanet.domain.event.EventStatus
import pt.isel.keepmyplanet.domain.message.ChatInfo
import pt.isel.keepmyplanet.ui.components.AppTopBar
import pt.isel.keepmyplanet.ui.components.ConfirmActionDialog
import pt.isel.keepmyplanet.ui.components.DetailCard
import pt.isel.keepmyplanet.ui.components.ErrorState
import pt.isel.keepmyplanet.ui.components.FullScreenLoading
import pt.isel.keepmyplanet.ui.components.InfoRow
import pt.isel.keepmyplanet.ui.components.LoadingButton
import pt.isel.keepmyplanet.ui.components.LoadingOutlinedButton
import pt.isel.keepmyplanet.ui.components.QrCodeIconButton
import pt.isel.keepmyplanet.ui.components.isQrScanningAvailable
import pt.isel.keepmyplanet.ui.event.details.components.CleanlinessConfirmationDialog
import pt.isel.keepmyplanet.ui.event.details.components.ParticipantRow
import pt.isel.keepmyplanet.ui.event.details.components.ParticipantSelectionDialog
import pt.isel.keepmyplanet.ui.event.details.components.TransferOwnershipBanner
import pt.isel.keepmyplanet.ui.event.details.states.EventDetailsEvent
import pt.isel.keepmyplanet.ui.event.details.states.EventDetailsUiState
import pt.isel.keepmyplanet.utils.toFormattedString

@Composable
fun EventDetailsScreen(
    viewModel: EventDetailsViewModel,
    eventId: Id,
    onNavigateToChat: (ChatInfo) -> Unit,
    onNavigateToEditEvent: (Id) -> Unit,
    onNavigateToManageAttendance: (Id) -> Unit,
    onNavigateToMyQrCode: (userId: Id, organizerName: String) -> Unit,
    onNavigateToStatusHistory: (Id) -> Unit,
    onNavigateToUpdateZone: (Id) -> Unit,
    onNavigateToParticipantList: (Id) -> Unit,
    onNavigateBack: () -> Unit,
) {
    val uiState by viewModel.uiState.collectAsState()
    val event = uiState.event
    val snackbarHostState = remember { SnackbarHostState() }
    val showCancelDialog = remember { mutableStateOf(false) }
    val showDeleteDialog = remember { mutableStateOf(false) }
    val showCleanlinessDialog = remember { mutableStateOf(false) }
    val showTransferDialog = remember { mutableStateOf(false) }

    if (uiState.showCleanlinessConfirmation && event != null) {
        LaunchedEffect(uiState.showCleanlinessConfirmation) {
            showCleanlinessDialog.value = true
        }
    }

    if (showCancelDialog.value) {
        ConfirmActionDialog(
            showDialog = showCancelDialog,
            onConfirm = { viewModel.changeEventStatus(EventStatus.CANCELLED) },
            title = "Cancel Event?",
            text = "Are you sure you want to cancel this event? This action cannot be undone.",
        )
    }
    if (showDeleteDialog.value) {
        ConfirmActionDialog(
            showDialog = showDeleteDialog,
            onConfirm = viewModel::deleteEvent,
            title = "Delete Event?",
            text =
                "Are you sure you want to permanently delete this event? " +
                    "All associated data, including chat history, will be lost.",
        )
    }

    if (showCleanlinessDialog.value) {
        CleanlinessConfirmationDialog(
            onConfirm = {
                viewModel.confirmZoneCleanliness(wasCleaned = true)
                showCleanlinessDialog.value = false
            },
            onDismiss = {
                viewModel.confirmZoneCleanliness(wasCleaned = false)
                showCleanlinessDialog.value = false
            },
            onDismissRequest = { showCleanlinessDialog.value = false },
        )
    }

    if (showTransferDialog.value) {
        val potentialNominees = uiState.participants.filter { it.id != uiState.event?.organizerId }
        ParticipantSelectionDialog(
            participants = potentialNominees,
            onParticipantSelected = { nomineeId ->
                viewModel.initiateTransfer(nomineeId)
                showTransferDialog.value = false
            },
            onDismiss = { showTransferDialog.value = false },
        )
    }

    LaunchedEffect(viewModel.events) {
        viewModel.events.collectLatest { event ->
            when (event) {
                is EventDetailsEvent.ShowSnackbar -> {
                    snackbarHostState.showSnackbar(
                        message = event.message,
                        duration = SnackbarDuration.Short,
                    )
                }

                is EventDetailsEvent.EventDeleted -> {
                    onNavigateBack()
                }

                is EventDetailsEvent.NavigateBack -> {
                    onNavigateBack()
                }

                is EventDetailsEvent.NavigateToManageAttendance -> {
                    onNavigateToManageAttendance(event.eventId)
                }

                is EventDetailsEvent.NavigateToMyQrCode -> {
                    onNavigateToMyQrCode(event.userId, event.organizerName)
                }

                is EventDetailsEvent.NavigateToUpdateZone -> onNavigateToUpdateZone(event.zoneId)

                is EventDetailsEvent.ShowParticipantSelectionDialog -> {
                    showTransferDialog.value = true
                }
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
                    if (uiState.canUseQrFeature && isQrScanningAvailable) {
                        QrCodeIconButton(
                            onClick = viewModel::onQrCodeIconClicked,
                            contentDescription = "Open QR Code Feature",
                        )
                    }
                },
            )
        },
    ) { paddingValues ->
        Box(modifier = Modifier.fillMaxSize().padding(paddingValues)) {
            when {
                uiState.isLoading -> FullScreenLoading()
                uiState.error != null -> {
                    ErrorState(message = uiState.error!!) {
                        viewModel.loadEventDetails(eventId)
                    }
                }

                event != null -> {
                    val organizer = uiState.participants.find { it.id == event.organizerId }
                    Column(
                        modifier =
                            Modifier
                                .fillMaxSize()
                                .verticalScroll(rememberScrollState())
                                .padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp),
                    ) {
                        if (uiState.isCurrentUserPendingNominee) {
                            TransferOwnershipBanner(
                                isLoading = uiState.actionState == EventDetailsUiState.ActionState.RESPONDING_TO_TRANSFER,
                                onAccept = { viewModel.respondToTransfer(true) },
                                onDecline = { viewModel.respondToTransfer(false) },
                            )
                        }

                        DetailCard(title = "Description") {
                            Text(
                                text = event.description.value,
                                style = MaterialTheme.typography.bodyMedium,
                            )
                        }

                        DetailCard(title = "Information") {
                            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                                organizer?.let {
                                    InfoRow(
                                        icon = Icons.Default.Person,
                                        text = "Organizer: ${it.name.value}",
                                    )
                                }

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

                                InfoRow(
                                    icon = Icons.Default.Flag,
                                    text = "Status: ${event.status}",
                                    onClick = {
                                        onNavigateToStatusHistory(event.id)
                                    },
                                    isClickable = true,
                                )

                                InfoRow(
                                    icon = Icons.Default.Schedule,
                                    text = "Created: ${event.createdAt.toFormattedString()}",
                                )
                                InfoRow(
                                    icon = Icons.Default.Schedule,
                                    text = "Last update: ${event.updatedAt.toFormattedString()}",
                                )
                            }
                        }
                        if (uiState.participants.isNotEmpty()) {
                            val maxParticipantsText = event.maxParticipants?.let { "/$it" } ?: ""
                            DetailCard(
                                "Participants (${uiState.participants.size}$maxParticipantsText)",
                            ) {
                                Column(modifier = Modifier.fillMaxWidth()) {
                                    val participantsPreview = uiState.participants.take(5)
                                    participantsPreview.forEach { participant ->
                                        ParticipantRow(
                                            participant = participant,
                                            isOrganizer = participant.id == event.organizerId,
                                        )
                                    }

                                    if (uiState.participants.size > 5) {
                                        TextButton(
                                            onClick = { onNavigateToParticipantList(event.id) },
                                            modifier = Modifier.align(Alignment.CenterHorizontally),
                                        ) {
                                            Text("View All (${uiState.participants.size})")
                                        }
                                    }
                                }
                            }
                        }

                        if (uiState.canUserJoin ||
                            uiState.canUserLeave ||
                            uiState.canUserAccessChat
                        ) {
                            DetailCard(title = "Actions") {
                                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                    if (uiState.canUserJoin) {
                                        LoadingButton(
                                            onClick = viewModel::joinEvent,
                                            isLoading =
                                                uiState.actionState ==
                                                    EventDetailsUiState.ActionState.JOINING,
                                            enabled = !uiState.isActionInProgress,
                                            text = "Join Event",
                                            modifier = Modifier.fillMaxWidth(),
                                        )
                                    }
                                    if (uiState.canUserLeave) {
                                        LoadingButton(
                                            onClick = viewModel::leaveEvent,
                                            isLoading =
                                                uiState.actionState ==
                                                    EventDetailsUiState.ActionState.LEAVING,
                                            enabled = !uiState.isActionInProgress,
                                            text = "Leave Event",
                                            modifier = Modifier.fillMaxWidth(),
                                        )
                                    }
                                    if (uiState.canUserAccessChat) {
                                        Button(
                                            onClick = {
                                                onNavigateToChat(
                                                    ChatInfo(event.id, event.title),
                                                )
                                            },
                                            enabled = !uiState.isActionInProgress,
                                            modifier = Modifier.fillMaxWidth(),
                                        ) {
                                            val chatButtonText =
                                                if (uiState.isChatReadOnly) {
                                                    "View Chat History"
                                                } else {
                                                    "Open Chat"
                                                }
                                            Text(chatButtonText)
                                        }
                                    }
                                }
                            }
                        }

                        if (uiState.isCurrentUserOrganizer) {
                            DetailCard(title = "Organizer Actions") {
                                Column(
                                    modifier = Modifier.fillMaxWidth(),
                                    verticalArrangement = Arrangement.spacedBy(8.dp),
                                ) {
                                    if (uiState.canUserEdit) {
                                        Button(
                                            onClick = { onNavigateToEditEvent(event.id) },
                                            enabled = !uiState.isActionInProgress,
                                            modifier = Modifier.fillMaxWidth(),
                                        ) { Text("Edit Event Details") }
                                    }
                                    if (uiState.canOrganizerComplete) {
                                        LoadingButton(
                                            onClick = {
                                                viewModel.changeEventStatus(
                                                    EventStatus.COMPLETED,
                                                )
                                            },
                                            isLoading =
                                                uiState.actionState ==
                                                    EventDetailsUiState.ActionState.COMPLETING,
                                            enabled = !uiState.isActionInProgress,
                                            text = "Mark as Completed",
                                            modifier = Modifier.fillMaxWidth(),
                                        )
                                    }
                                    if (uiState.canOrganizerCancel) {
                                        LoadingOutlinedButton(
                                            onClick = { showCancelDialog.value = true },
                                            modifier = Modifier.fillMaxWidth(),
                                            colors =
                                                ButtonDefaults.outlinedButtonColors(
                                                    contentColor = MaterialTheme.colorScheme.error,
                                                ),
                                            enabled = !uiState.isActionInProgress,
                                            isLoading =
                                                uiState.actionState ==
                                                    EventDetailsUiState.ActionState.CANCELLING,
                                            text = "Cancel Event",
                                            loadingIndicatorColor = MaterialTheme.colorScheme.error,
                                        )
                                    }
                                    if (uiState.canOrganizerDelete) {
                                        LoadingOutlinedButton(
                                            onClick = { showDeleteDialog.value = true },
                                            modifier = Modifier.fillMaxWidth(),
                                            colors =
                                                ButtonDefaults.outlinedButtonColors(
                                                    contentColor = MaterialTheme.colorScheme.error,
                                                ),
                                            enabled = !uiState.isActionInProgress,
                                            isLoading =
                                                uiState.actionState ==
                                                    EventDetailsUiState.ActionState.DELETING,
                                            text = "Delete Event",
                                            loadingIndicatorColor = MaterialTheme.colorScheme.error,
                                        )
                                    }

                                    if (uiState.canTransferOwnership) {
                                        LoadingButton(
                                            onClick = viewModel::onTransferOwnershipClicked,
                                            isLoading = uiState.actionState == EventDetailsUiState.ActionState.INITIATING_TRANSFER,
                                            enabled = !uiState.isActionInProgress,
                                            text = "Transfer Ownership",
                                            modifier = Modifier.fillMaxWidth(),
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
