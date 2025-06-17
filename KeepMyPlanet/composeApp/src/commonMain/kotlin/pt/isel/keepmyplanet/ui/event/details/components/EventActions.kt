package pt.isel.keepmyplanet.ui.event.details.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material.Button
import androidx.compose.material.ButtonDefaults
import androidx.compose.material.Divider
import androidx.compose.material.MaterialTheme
import androidx.compose.material.OutlinedButton
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import pt.isel.keepmyplanet.domain.common.Id
import pt.isel.keepmyplanet.ui.chat.model.ChatInfo
import pt.isel.keepmyplanet.ui.components.LoadingButton
import pt.isel.keepmyplanet.ui.event.details.model.EventDetailsUiState

@Suppress("ktlint:standard:function-naming")
@Composable
fun EventActions(
    uiState: EventDetailsUiState,
    currentUserId: Id,
    onJoinEvent: () -> Unit,
    onLeaveEvent: () -> Unit,
    onNavigateToChat: (ChatInfo) -> Unit,
    onNavigateToEditEvent: () -> Unit,
    onCancelEvent: () -> Unit,
    onCompleteEvent: () -> Unit,
    onDeleteEvent: () -> Unit,
) {
    val event = uiState.event ?: return
    val showCancelDialog = remember { mutableStateOf(false) }
    val showDeleteDialog = remember { mutableStateOf(false) }

    if (showCancelDialog.value) {
        ConfirmActionDialog(
            showDialog = showCancelDialog,
            onConfirm = onCancelEvent,
            title = "Cancel Event?",
            text = "Are you sure you want to cancel this event? This action cannot be undone.",
        )
    }
    if (showDeleteDialog.value) {
        ConfirmActionDialog(
            showDialog = showDeleteDialog,
            onConfirm = onDeleteEvent,
            title = "Delete Event?",
            text = "Are you sure you want to permanently delete this event? All associated data, including chat history, will be lost.",
        )
    }

    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        if (uiState.canUserJoin(currentUserId)) {
            LoadingButton(
                onClick = onJoinEvent,
                isLoading = uiState.isJoining,
                text = "Join Event",
                modifier = Modifier.fillMaxWidth(),
            )
        }
        if (uiState.canUserLeave(currentUserId)) {
            LoadingButton(
                onClick = onLeaveEvent,
                isLoading = uiState.isLeaving,
                text = "Leave Event",
                modifier = Modifier.fillMaxWidth(),
            )
        }
        if (uiState.canUserAccessChat(currentUserId)) {
            Button(
                onClick = { onNavigateToChat(ChatInfo(event.id, event.title)) },
                modifier = Modifier.fillMaxWidth(),
            ) {
                val chatButtonText =
                    if (uiState.isChatReadOnly) "View Chat History" else "Open Chat"
                Text(chatButtonText)
            }
        }

        if (uiState.isUserOrganizer(currentUserId)) {
            Spacer(modifier = Modifier.height(16.dp))
            Divider()
            Spacer(modifier = Modifier.height(16.dp))
            Text("Organizer Actions", style = MaterialTheme.typography.overline)

            if (uiState.canUserEdit(currentUserId)) {
                Button(
                    onClick = onNavigateToEditEvent,
                    modifier = Modifier.fillMaxWidth(),
                ) { Text("Edit Event Details") }
            }
            if (uiState.canOrganizerComplete(currentUserId)) {
                LoadingButton(
                    onClick = onCompleteEvent,
                    isLoading = uiState.isCompleting,
                    text = "Mark as Completed",
                    modifier = Modifier.fillMaxWidth(),
                )
            }

            if (uiState.canOrganizerCancel(currentUserId)) {
                OutlinedButton(
                    onClick = { showCancelDialog.value = true },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colors.error),
                    enabled = !uiState.isCancelling,
                ) {
                    Text(if (uiState.isCancelling) "Cancelling..." else "Cancel Event")
                }
            }
            if (uiState.canOrganizerDelete(currentUserId)) {
                OutlinedButton(
                    onClick = { showDeleteDialog.value = true },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colors.error),
                    enabled = !uiState.isDeleting,
                ) {
                    Text(if (uiState.isDeleting) "Deleting..." else "Delete Event")
                }
            }
        }
    }
}
