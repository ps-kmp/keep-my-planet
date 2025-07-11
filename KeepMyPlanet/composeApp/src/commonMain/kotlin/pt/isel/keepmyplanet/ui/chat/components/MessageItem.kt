package pt.isel.keepmyplanet.ui.chat.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import pt.isel.keepmyplanet.ui.chat.states.SendStatus
import pt.isel.keepmyplanet.ui.chat.states.UiMessage
import pt.isel.keepmyplanet.ui.theme.primaryLight
import pt.isel.keepmyplanet.utils.formatTimestamp

@Composable
fun MessageItem(
    uiMessage: UiMessage,
    currentUserId: UInt,
    onRetry: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val message = uiMessage.message
    val isCurrentUser = message.senderId.value == currentUserId

    val alignment = if (isCurrentUser) Alignment.CenterEnd else Alignment.CenterStart
    val backgroundColor = if (isCurrentUser) primaryLight else MaterialTheme.colorScheme.surface
    val textColor =
        if (isCurrentUser) {
            MaterialTheme.colorScheme.onPrimary
        } else {
            MaterialTheme.colorScheme.onSurface
        }
    val startPadding = if (isCurrentUser) 48.dp else 8.dp
    val endPadding = if (isCurrentUser) 8.dp else 48.dp

    val alpha by animateFloatAsState(if (uiMessage.status == SendStatus.SENDING) 0.6f else 1f)

    Box(modifier = modifier.fillMaxWidth().padding(start = startPadding, end = endPadding)) {
        Surface(
            modifier = Modifier.align(alignment).alpha(alpha),
            shape = MaterialTheme.shapes.medium,
            color = backgroundColor,
            shadowElevation = 1.dp,
        ) {
            Column(modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)) {
                if (!isCurrentUser) {
                    Text(
                        text = message.senderName.value,
                        style = MaterialTheme.typography.bodySmall,
                        fontWeight = FontWeight.Bold,
                        color = textColor.copy(alpha = 0.8f),
                        modifier = Modifier.padding(bottom = 2.dp),
                    )
                }
                Text(
                    text = message.content.value,
                    style = MaterialTheme.typography.bodyMedium,
                    color = textColor,
                )
                Spacer(modifier = Modifier.height(4.dp))
                Row(
                    modifier = Modifier.align(Alignment.End),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    when (uiMessage.status) {
                        SendStatus.SENDING -> {
                            Icon(
                                imageVector = Icons.Default.Schedule,
                                contentDescription = "Sending",
                                modifier = Modifier.size(14.dp).padding(end = 4.dp),
                                tint = textColor.copy(alpha = 0.7f),
                            )
                        }

                        SendStatus.FAILED -> {
                            IconButton(onClick = onRetry, modifier = Modifier.size(20.dp)) {
                                Icon(
                                    imageVector = Icons.Default.Refresh,
                                    contentDescription = "Retry sending",
                                    tint = MaterialTheme.colorScheme.error,
                                )
                            }
                            Icon(
                                imageVector = Icons.Default.Error,
                                contentDescription = "Failed to send",
                                modifier = Modifier.size(14.dp).padding(end = 4.dp),
                                tint = MaterialTheme.colorScheme.error,
                            )
                        }

                        SendStatus.SENT -> { // No icon needed
                        }
                    }
                    Text(
                        text = formatTimestamp(message.timestamp),
                        style = MaterialTheme.typography.bodySmall,
                        color = textColor.copy(alpha = 0.7f),
                    )
                }
            }
        }
    }
}
