package pt.isel.keepmyplanet.ui.event.details.components

import androidx.compose.material.AlertDialog
import androidx.compose.material.Text
import androidx.compose.material.Button
import androidx.compose.runtime.Composable

@Composable
fun CleanlinessConfirmationDialog(
    onConfirm: () -> Unit,
    onDismiss: () -> Unit,
    onDismissRequest: () -> Unit
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
        }
    )
}
