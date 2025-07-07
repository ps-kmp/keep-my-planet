package pt.isel.keepmyplanet.ui.event.details.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.dp
import pt.isel.keepmyplanet.ui.components.LoadingButton

@Composable
fun SendNotificationDialog(
    title: String,
    message: String,
    onTitleChange: (String) -> Unit,
    onMessageChange: (String) -> Unit,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit,
    isLoading: Boolean,
    isSendEnabled: Boolean,
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Send Notification") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value = title,
                    onValueChange = onTitleChange,
                    label = { Text("Title") },
                    singleLine = true,
                )
                OutlinedTextField(
                    value = message,
                    onValueChange = onMessageChange,
                    label = { Text("Message") },
                    minLines = 3,
                )
            }
        },
        confirmButton = {
            LoadingButton(
                onClick = onConfirm,
                isLoading = isLoading,
                enabled = isSendEnabled,
                text = "Send",
            )
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel") } },
    )
}
