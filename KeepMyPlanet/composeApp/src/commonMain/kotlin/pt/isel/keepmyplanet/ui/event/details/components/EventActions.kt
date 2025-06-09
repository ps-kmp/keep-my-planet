package pt.isel.keepmyplanet.ui.event.details.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material.Button
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import pt.isel.keepmyplanet.domain.common.Id
import pt.isel.keepmyplanet.ui.chat.model.ChatInfo
import pt.isel.keepmyplanet.ui.components.LoadingButton
import pt.isel.keepmyplanet.ui.event.model.EventDetailsUiState

@Suppress("ktlint:standard:function-naming")
@Composable
fun EventActions(
    uiState: EventDetailsUiState,
    currentUserId: Id,
    onJoinEvent: () -> Unit,
    onLeaveEvent: () -> Unit,
    onNavigateToChat: (ChatInfo) -> Unit,
    onNavigateToEditEvent: () -> Unit,
) {
    val event = uiState.event ?: return

    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        if (uiState.canUserEdit(currentUserId)) {
            Button(
                onClick = onNavigateToEditEvent,
                modifier = Modifier.fillMaxWidth(),
            ) {
                Text("Edit Event")
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            when {
                uiState.canUserJoin(currentUserId) -> {
                    LoadingButton(
                        onClick = onJoinEvent,
                        isLoading = uiState.isJoining,
                        text = "Join Event",
                        modifier = Modifier.weight(1f),
                    )
                }

                uiState.canUserLeave(currentUserId) -> {
                    LoadingButton(
                        onClick = onLeaveEvent,
                        isLoading = uiState.isLeaving,
                        text = "Leave Event",
                        modifier = Modifier.weight(1f),
                    )
                }
            }

            if (uiState.canUserChat(currentUserId)) {
                Button(
                    onClick = { onNavigateToChat(ChatInfo(event.id, event.title)) },
                    modifier = Modifier.weight(1f),
                ) {
                    Text("Chat")
                }
            }
        }
    }
}
