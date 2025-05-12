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
import androidx.compose.material.MaterialTheme
import androidx.compose.material.OutlinedTextField
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import pt.isel.keepmyplanet.ui.screens.user.UserProfileUiState

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
            Text("Change Your Password", style = MaterialTheme.typography.h4)
            OutlinedTextField(
                value = uiState.oldPasswordInput,
                onValueChange = onOldPasswordChanged,
                label = { Text("Old Password") },
                modifier = Modifier.fillMaxWidth(),
                visualTransformation = PasswordVisualTransformation(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                singleLine = true,
                enabled = !uiState.isChangingPassword,
            )
            OutlinedTextField(
                value = uiState.newPasswordInput,
                onValueChange = onNewPasswordChanged,
                label = { Text("New Password") },
                modifier = Modifier.fillMaxWidth(),
                visualTransformation = PasswordVisualTransformation(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                singleLine = true,
                enabled = !uiState.isChangingPassword,
            )
            OutlinedTextField(
                value = uiState.confirmPasswordInput,
                onValueChange = onConfirmPasswordChanged,
                label = { Text("Confirm New Password") },
                modifier = Modifier.fillMaxWidth(),
                visualTransformation = PasswordVisualTransformation(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                singleLine = true,
                enabled = !uiState.isChangingPassword,
            )
            Button(
                onClick = onChangePasswordClicked,
                enabled = uiState.isChangePasswordEnabled,
                modifier = Modifier.fillMaxWidth(),
            ) {
                if (uiState.isChangingPassword) {
                    CircularProgressIndicator(modifier = Modifier.size(24.dp))
                } else {
                    Text("Confirm Password Change")
                }
            }
        }
    }
}
