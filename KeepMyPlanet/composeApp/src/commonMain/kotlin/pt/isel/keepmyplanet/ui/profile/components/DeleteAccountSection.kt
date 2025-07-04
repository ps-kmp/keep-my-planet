package pt.isel.keepmyplanet.ui.profile.components

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import pt.isel.keepmyplanet.ui.profile.states.UserProfileUiState

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
                            containerColor = MaterialTheme.colorScheme.error,
                            contentColor = MaterialTheme.colorScheme.onError,
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
        colors =
            ButtonDefaults.outlinedButtonColors(
                containerColor = MaterialTheme.colorScheme.errorContainer,
                contentColor = MaterialTheme.colorScheme.onErrorContainer,
            ),
    ) {
        if (uiState.actionState == UserProfileUiState.ActionState.DELETING_ACCOUNT) {
            CircularProgressIndicator(
                modifier = Modifier.size(24.dp),
                color = MaterialTheme.colorScheme.onErrorContainer,
            )
        } else {
            Text("Delete Account")
        }
    }
}
