package pt.isel.keepmyplanet.ui.chat.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import pt.isel.keepmyplanet.ui.components.LoadingIconButton

@Composable
fun MessageInput(
    message: String,
    onMessageChange: (String) -> Unit,
    onSendClick: () -> Unit,
    isSending: Boolean,
    sendEnabled: Boolean,
    maxLength: Int,
    errorText: String? = null,
) {
    val isError = errorText != null
    Surface(
        shadowElevation = 4.dp,
        modifier = Modifier.fillMaxWidth(),
    ) {
        Column(modifier = Modifier.padding(horizontal = 8.dp, vertical = 8.dp)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
            ) {
                OutlinedTextField(
                    value = message,
                    onValueChange = { if (it.length <= maxLength) onMessageChange(it) },
                    modifier = Modifier.weight(1f),
                    placeholder = { Text("Type a message...") },
                    enabled = !isSending,
                    isError = isError,
                    maxLines = 5,
                    shape = MaterialTheme.shapes.medium,
                )
                Spacer(modifier = Modifier.width(8.dp))
                LoadingIconButton(
                    onClick = onSendClick,
                    isLoading = isSending,
                    enabled = sendEnabled,
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.Send,
                        contentDescription = "Send Message",
                        tint =
                            if (sendEnabled) {
                                MaterialTheme.colorScheme.primary
                            } else {
                                MaterialTheme.colorScheme.onSurface.copy(0.38f)
                            },
                    )
                }
            }
            Row(
                modifier = Modifier.fillMaxWidth().padding(top = 4.dp, end = 56.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                if (isError) {
                    Text(
                        text = errorText,
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodySmall,
                        modifier = Modifier.weight(1f).padding(start = 12.dp),
                    )
                } else {
                    Spacer(modifier = Modifier.weight(1f))
                }
                Text(
                    text = "${message.length} / $maxLength",
                    style = MaterialTheme.typography.bodySmall,
                    textAlign = TextAlign.End,
                    color =
                        if (message.length > maxLength) {
                            MaterialTheme.colorScheme.error
                        } else {
                            Color.Unspecified
                        },
                )
            }
        }
    }
}
