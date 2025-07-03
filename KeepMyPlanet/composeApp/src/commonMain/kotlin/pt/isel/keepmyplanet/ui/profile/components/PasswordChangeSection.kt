package pt.isel.keepmyplanet.ui.profile.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import pt.isel.keepmyplanet.ui.components.FormField
import pt.isel.keepmyplanet.ui.components.LoadingButton
import pt.isel.keepmyplanet.ui.profile.states.UserProfileUiState

@Composable
fun PasswordChangeSection(
    uiState: UserProfileUiState,
    onOldPasswordChanged: (String) -> Unit,
    onNewPasswordChanged: (String) -> Unit,
    onConfirmPasswordChanged: (String) -> Unit,
    onChangePasswordClicked: () -> Unit,
) {
    val isActionInProgress = uiState.actionState != UserProfileUiState.ActionState.IDLE

    AnimatedVisibility(visible = uiState.showPasswordChangeSection) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Text("Change Your Password", style = MaterialTheme.typography.titleSmall)
            FormField(
                value = uiState.oldPasswordInput,
                onValueChange = onOldPasswordChanged,
                label = "Old Password",
                isPasswordField = true,
                singleLine = true,
                enabled = !isActionInProgress,
            )
            FormField(
                value = uiState.newPasswordInput,
                onValueChange = onNewPasswordChanged,
                label = "New Password",
                isPasswordField = true,
                singleLine = true,
                enabled = !isActionInProgress,
                errorText = uiState.newPasswordInputError,
            )
            FormField(
                value = uiState.confirmPasswordInput,
                onValueChange = onConfirmPasswordChanged,
                label = "Confirm New Password",
                isPasswordField = true,
                singleLine = true,
                enabled = !isActionInProgress,
                errorText = uiState.confirmPasswordInputError,
            )
            LoadingButton(
                onClick = onChangePasswordClicked,
                enabled = uiState.isChangePasswordEnabled,
                isLoading = uiState.actionState == UserProfileUiState.ActionState.CHANGING_PASSWORD,
                text = "Confirm Password Change",
                modifier = Modifier.fillMaxWidth(),
            )
        }
    }
}
