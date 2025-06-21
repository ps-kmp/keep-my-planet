package pt.isel.keepmyplanet.ui.user.profile.components

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.material.AlertDialog
import androidx.compose.material.Button
import androidx.compose.material.ButtonDefaults
import androidx.compose.material.CircularProgressIndicator
import androidx.compose.material.MaterialTheme
import androidx.compose.material.OutlinedButton
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import pt.isel.keepmyplanet.ui.user.profile.model.UserProfileUiState

@Composable
fun DeleteAccountSection(
    uiState: UserProfileUiState,
    onConfirmDelete: () -> Unit,
) {
    val showDeleteConfirmDialog = remember { mutableStateOf(false) }

    if (showDeleteConfirmDialog.value) {
        AlertDialog(
            onDismissRequest = { showDeleteConfirmDialog.value = false },
            title = { Text("Delete Account?") },
            text = { Text("Are you sure you want to permanently delete your account?") },
            confirmButton = {
                Button(
                    onClick = {
                        showDeleteConfirmDialog.value = false
                        onConfirmDelete()
                    },
                    colors =
                        ButtonDefaults.buttonColors(
                            backgroundColor = MaterialTheme.colors.error,
                        ),
                ) {
                    Text("Delete")
                }
            },
            dismissButton = {
                OutlinedButton(onClick = { showDeleteConfirmDialog.value = false }) {
                    Text("Cancel")
                }
            },
        )
    }

    OutlinedButton(
        onClick = { showDeleteConfirmDialog.value = true },
        enabled = uiState.isDeleteAccountEnabled,
        modifier = Modifier.fillMaxWidth(),
        colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colors.error),
    ) {
        if (uiState.isDeletingAccount) {
            CircularProgressIndicator(
                modifier = Modifier.size(24.dp),
                color = MaterialTheme.colors.error,
            )
        } else {
            Text("Delete Account")
        }
    }
}
