@file:Suppress("ktlint:standard:function-naming")

package pt.isel.keepmyplanet.ui.screens.user.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.Button
import androidx.compose.material.CircularProgressIndicator
import androidx.compose.material.OutlinedTextField
import androidx.compose.material.Text
import androidx.compose.material.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import pt.isel.keepmyplanet.ui.screens.user.UserProfileUiState

@Composable
fun ProfileInfoSection(
    uiState: UserProfileUiState,
    onNameChanged: (String) -> Unit,
    onEmailChanged: (String) -> Unit,
    onEditProfileToggled: () -> Unit,
    onSaveProfileClicked: () -> Unit,
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        TextButton(onClick = onEditProfileToggled) {
            Text(if (uiState.isEditingProfile) "Cancel Edit" else "Edit Profile")
        }

        if (uiState.isEditingProfile) {
            OutlinedTextField(
                value = uiState.nameInput,
                onValueChange = onNameChanged,
                label = { Text("Name") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                enabled = !uiState.isUpdatingProfile,
            )
        } else {
            Text("Name: ${uiState.userDetails?.username ?: "N/A"}")
        }

        if (uiState.isEditingProfile) {
            OutlinedTextField(
                value = uiState.emailInput,
                onValueChange = onEmailChanged,
                label = { Text("Email") },
                modifier = Modifier.fillMaxWidth(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                singleLine = true,
                enabled = !uiState.isUpdatingProfile,
            )
        } else {
            Text("Email: ${uiState.userDetails?.email ?: "N/A"}")
        }

        AnimatedVisibility(visible = uiState.isEditingProfile) {
            Button(
                onClick = onSaveProfileClicked,
                enabled = uiState.isSaveProfileEnabled,
                modifier = Modifier.fillMaxWidth(),
            ) {
                if (uiState.isUpdatingProfile) {
                    CircularProgressIndicator(modifier = Modifier.size(24.dp))
                } else {
                    Text("Save Changes")
                }
            }
        }
    }
}
