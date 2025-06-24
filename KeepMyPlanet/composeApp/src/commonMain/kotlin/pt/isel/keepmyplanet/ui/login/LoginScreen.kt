package pt.isel.keepmyplanet.ui.login

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
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
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.flow.collectLatest
import pt.isel.keepmyplanet.dto.user.UserSession
import pt.isel.keepmyplanet.ui.components.FormField
import pt.isel.keepmyplanet.ui.components.LoadingButton
import pt.isel.keepmyplanet.ui.login.model.LoginEvent
import pt.isel.keepmyplanet.ui.login.model.LoginUiState

@Composable
fun LoginScreen(
    viewModel: LoginViewModel,
    onNavigateToRegister: () -> Unit,
    onLoginSuccess: (UserSession) -> Unit,
) {
    val uiState by viewModel.uiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(viewModel.events) {
        viewModel.events.collectLatest { event ->
            when (event) {
                is LoginEvent.ShowSnackbar -> {
                    snackbarHostState.showSnackbar(
                        message = event.message,
                        duration = SnackbarDuration.Short,
                    )
                }

                is LoginEvent.LoginSuccess -> {
                    onLoginSuccess(event.userSession)
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
            LoginContent(
                uiState = uiState,
                onEmailChanged = viewModel::onEmailChanged,
                onPasswordChanged = viewModel::onPasswordChanged,
                onLoginClicked = viewModel::onLoginClicked,
                onNavigateToRegister = onNavigateToRegister,
            )
        }
    }
}

@Composable
private fun LoginContent(
    uiState: LoginUiState,
    onEmailChanged: (String) -> Unit,
    onPasswordChanged: (String) -> Unit,
    onLoginClicked: () -> Unit,
    onNavigateToRegister: () -> Unit,
) {
    val isActionInProgress = uiState.actionState is LoginUiState.ActionState.LoggingIn

    Column(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        Text(text = "Login", style = MaterialTheme.typography.h4)

        FormField(
            value = uiState.email,
            onValueChange = onEmailChanged,
            label = "Email",
            singleLine = true,
            enabled = !isActionInProgress,
            errorText = uiState.emailError,
        )

        FormField(
            value = uiState.password,
            onValueChange = onPasswordChanged,
            label = "Password",
            singleLine = true,
            enabled = !isActionInProgress,
            isPasswordField = true,
        )

        LoadingButton(
            onClick = onLoginClicked,
            enabled = uiState.isLoginEnabled,
            isLoading = isActionInProgress,
            text = "Login",
            modifier = Modifier.fillMaxWidth().height(48.dp),
        )

        TextButton(onClick = onNavigateToRegister) {
            Text("Don't have an account? Register")
        }
    }
}
