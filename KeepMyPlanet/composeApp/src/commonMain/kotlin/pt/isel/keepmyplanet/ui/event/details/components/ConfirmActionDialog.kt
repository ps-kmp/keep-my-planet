package pt.isel.keepmyplanet.ui.event.details.components

import androidx.compose.material.AlertDialog
import androidx.compose.material.Button
import androidx.compose.material.ButtonDefaults
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.material.TextButton
import androidx.compose.runtime.Composable

@Suppress("ktlint:standard:function-naming")
@Composable
fun ConfirmActionDialog(
    showDialog: androidx.compose.runtime.MutableState<Boolean>,
    onConfirm: () -> Unit,
    title: String,
    text: String,
) {
    AlertDialog(
        onDismissRequest = { showDialog.value = false },
        title = { Text(title) },
        text = { Text(text) },
        confirmButton = {
            Button(
                onClick = {
                    onConfirm()
                    showDialog.value = false
                },
                colors = ButtonDefaults.buttonColors(backgroundColor = MaterialTheme.colors.error),
            ) {
                Text("Confirm")
            }
        },
        dismissButton = {
            TextButton(onClick = { showDialog.value = false }) {
                Text("Cancel")
            }
        },
    )
}
