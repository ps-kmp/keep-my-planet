package pt.isel.keepmyplanet.ui.admin.components

import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import pt.isel.keepmyplanet.domain.user.UserInfo
import pt.isel.keepmyplanet.domain.user.UserRole

@Composable
fun RoleChangeDialog(
    user: UserInfo,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit,
) {
    val newRole = if (user.role == UserRole.USER) UserRole.ADMIN else UserRole.USER
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Change User Role") },
        text = {
            Text(
                "Are you sure you want to change ${user.name.value}'s " +
                    "role from ${user.role} to $newRole?",
            )
        },
        confirmButton = {
            Button(onClick = onConfirm) {
                Text("Confirm")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        },
    )
}
