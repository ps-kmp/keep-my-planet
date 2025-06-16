@file:Suppress("ktlint:standard:function-naming")

package pt.isel.keepmyplanet.ui.user.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.Text
import androidx.compose.material.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import pt.isel.keepmyplanet.ui.components.FormField
import pt.isel.keepmyplanet.ui.components.LoadingButton
import pt.isel.keepmyplanet.ui.user.model.UserProfileUiState

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
            FormField(
                value = uiState.nameInput,
                onValueChange = onNameChanged,
                label = "Name",
                singleLine = true,
                enabled = !uiState.isUpdatingProfile,
            )
        } else {
            Text("Name: ${uiState.userDetails?.name?.value ?: ""}")
        }

        if (uiState.isEditingProfile) {
            FormField(
                value = uiState.emailInput,
                onValueChange = onEmailChanged,
                label = "Email",
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                singleLine = true,
                enabled = !uiState.isUpdatingProfile,
            )
        } else {
            Text("Email: ${uiState.userDetails?.email?.value ?: "N/A"}")
        }

        AnimatedVisibility(visible = uiState.isEditingProfile) {
            LoadingButton(
                onClick = onSaveProfileClicked,
                enabled = uiState.isSaveProfileEnabled,
                isLoading = uiState.isUpdatingProfile,
                text = "Save Changes",
                modifier = Modifier.fillMaxWidth(),
            )
        }
    }
}
