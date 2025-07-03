package pt.isel.keepmyplanet.ui.event.details.components

import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable

@Composable
fun CleanlinessConfirmationDialog(
    onConfirm: () -> Unit,
    onDismiss: () -> Unit,
    onDismissRequest: () -> Unit,
) {
    AlertDialog(
        onDismissRequest = onDismissRequest,
        title = { Text("Confirm Zone Status") },
        text = { Text("The event is complete. Was the zone successfully cleaned?") },
        confirmButton = {
            Button(onClick = onConfirm) {
                Text("Yes, it's clean")
            }
        },
        dismissButton = {
            Button(onClick = onDismiss) {
                Text("No, still dirty")
            }
        },
    )
}
