@file:Suppress("ktlint:standard:function-naming")

package pt.isel.keepmyplanet.ui.user.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import pt.isel.keepmyplanet.ui.components.FormField
import pt.isel.keepmyplanet.ui.components.LoadingButton
import pt.isel.keepmyplanet.ui.user.model.UserProfileUiState

@Composable
fun PasswordChangeSection(
    uiState: UserProfileUiState,
    onOldPasswordChanged: (String) -> Unit,
    onNewPasswordChanged: (String) -> Unit,
    onConfirmPasswordChanged: (String) -> Unit,
    onChangePasswordClicked: () -> Unit,
) {
    AnimatedVisibility(visible = uiState.showPasswordChangeSection) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Text("Change Your Password", style = MaterialTheme.typography.h6)
            FormField(
                value = uiState.oldPasswordInput,
                onValueChange = onOldPasswordChanged,
                label = "Old Password",
                visualTransformation = PasswordVisualTransformation(),
                singleLine = true,
                enabled = !uiState.isChangingPassword,
            )
            FormField(
                value = uiState.newPasswordInput,
                onValueChange = onNewPasswordChanged,
                label = "New Password",
                visualTransformation = PasswordVisualTransformation(),
                singleLine = true,
                enabled = !uiState.isChangingPassword,
                errorText = uiState.newPasswordInputError,
            )
            FormField(
                value = uiState.confirmPasswordInput,
                onValueChange = onConfirmPasswordChanged,
                label = "Confirm New Password",
                visualTransformation = PasswordVisualTransformation(),
                singleLine = true,
                enabled = !uiState.isChangingPassword,
                errorText = uiState.confirmPasswordInputError,
            )
            LoadingButton(
                onClick = onChangePasswordClicked,
                enabled = uiState.isChangePasswordEnabled,
                isLoading = uiState.isChangingPassword,
                text = "Confirm Password Change",
                modifier = Modifier.fillMaxWidth(),
            )
        }
    }
}
