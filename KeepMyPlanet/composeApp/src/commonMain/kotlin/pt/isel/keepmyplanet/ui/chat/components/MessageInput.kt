package pt.isel.keepmyplanet.ui.chat.components

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material.ContentAlpha
import androidx.compose.material.Icon
import androidx.compose.material.MaterialTheme
import androidx.compose.material.OutlinedTextField
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.material.TextFieldDefaults
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import pt.isel.keepmyplanet.ui.components.LoadingIconButton

@Suppress("ktlint:standard:function-naming")
@Composable
fun MessageInput(
    message: String,
    onMessageChange: (String) -> Unit,
    onSendClick: () -> Unit,
    isSending: Boolean,
    sendEnabled: Boolean,
) {
    Surface(elevation = 4.dp, modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            OutlinedTextField(
                value = message,
                onValueChange = onMessageChange,
                modifier = Modifier.weight(1f),
                placeholder = { Text("Type a message...") },
                enabled = !isSending,
                maxLines = 5,
                colors =
                    TextFieldDefaults.outlinedTextFieldColors(
                        focusedBorderColor = MaterialTheme.colors.primary.copy(ContentAlpha.high),
                        unfocusedBorderColor = MaterialTheme.colors.onSurface.copy(ContentAlpha.disabled),
                    ),
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
                            MaterialTheme.colors.primary
                        } else {
                            MaterialTheme.colors.onSurface.copy(ContentAlpha.disabled)
                        },
                )
            }
        }
    }
}
