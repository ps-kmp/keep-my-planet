@file:Suppress("ktlint:standard:function-naming")

package pt.isel.keepmyplanet.ui.register

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Scaffold
import androidx.compose.material.SnackbarDuration
import androidx.compose.material.SnackbarHost
import androidx.compose.material.SnackbarHostState
import androidx.compose.material.Text
import androidx.compose.material.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import pt.isel.keepmyplanet.ui.components.FormField
import pt.isel.keepmyplanet.ui.components.LoadingButton
import pt.isel.keepmyplanet.ui.register.model.RegisterEvent
import pt.isel.keepmyplanet.ui.register.model.RegisterUiState

@Composable
fun RegisterScreen(
    viewModel: RegisterViewModel,
    onNavigateToLogin: () -> Unit,
) {
    val uiState by viewModel.uiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(viewModel, snackbarHostState) {
        viewModel.events.collect { event ->
            when (event) {
                is RegisterEvent.NavigateToLogin -> {
                    onNavigateToLogin()
                }

                is RegisterEvent.ShowSnackbar -> {
                    snackbarHostState.showSnackbar(
                        message = event.message,
                        duration = SnackbarDuration.Short,
                    )
                }
            }
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
    ) { paddingValues ->
        Box(
            modifier = Modifier.padding(paddingValues).fillMaxSize(),
            contentAlignment = Alignment.Center,
        ) {
            RegisterContent(
                uiState = uiState,
                onUsernameChanged = viewModel::onUsernameChanged,
                onEmailChanged = viewModel::onEmailChanged,
                onPasswordChanged = viewModel::onPasswordChanged,
                onConfirmPasswordChanged = viewModel::onConfirmPasswordChanged,
                onRegisterClicked = viewModel::onRegisterClicked,
                onNavigateToLogin = onNavigateToLogin,
            )
        }
    }
}

@Composable
private fun RegisterContent(
    uiState: RegisterUiState,
    onUsernameChanged: (String) -> Unit,
    onEmailChanged: (String) -> Unit,
    onPasswordChanged: (String) -> Unit,
    onConfirmPasswordChanged: (String) -> Unit,
    onRegisterClicked: () -> Unit,
    onNavigateToLogin: () -> Unit,
) {
    Column(
        modifier =
            Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp)
                .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Text(
            text = "Register",
            style = MaterialTheme.typography.h4,
        )

        FormField(
            value = uiState.username,
            onValueChange = onUsernameChanged,
            label = "Username",
            singleLine = true,
            enabled = !uiState.isLoading,
            errorText = uiState.usernameError,
        )

        FormField(
            value = uiState.email,
            onValueChange = onEmailChanged,
            label = "Email",
            singleLine = true,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
            enabled = !uiState.isLoading,
            errorText = uiState.emailError,
        )

        FormField(
            value = uiState.password,
            onValueChange = onPasswordChanged,
            label = "Password",
            singleLine = true,
            enabled = !uiState.isLoading,
            visualTransformation = PasswordVisualTransformation(),
            errorText = uiState.passwordError,
        )

        FormField(
            value = uiState.confirmPassword,
            onValueChange = onConfirmPasswordChanged,
            label = "Confirm Password",
            singleLine = true,
            enabled = !uiState.isLoading,
            visualTransformation = PasswordVisualTransformation(),
            errorText = uiState.confirmPasswordError,
        )

        LoadingButton(
            onClick = onRegisterClicked,
            enabled = uiState.canAttemptRegister,
            isLoading = uiState.isLoading,
            text = "Register",
            modifier = Modifier.fillMaxWidth().height(48.dp).padding(top = 8.dp),
        )

        TextButton(
            onClick = onNavigateToLogin,
            modifier = Modifier.padding(top = 8.dp),
        ) {
            Text("Already have an account? Login")
        }
    }
}
