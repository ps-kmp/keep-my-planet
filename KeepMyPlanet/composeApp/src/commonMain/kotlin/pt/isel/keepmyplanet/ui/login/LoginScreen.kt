package pt.isel.keepmyplanet.ui.login

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.flow.collectLatest
import pt.isel.keepmyplanet.domain.user.UserSession
import pt.isel.keepmyplanet.ui.components.FormApiError
import pt.isel.keepmyplanet.ui.components.FormField
import pt.isel.keepmyplanet.ui.components.LoadingButton
import pt.isel.keepmyplanet.ui.components.LoadingOutlinedButton
import pt.isel.keepmyplanet.ui.login.states.LoginEvent
import pt.isel.keepmyplanet.ui.login.states.LoginUiState

@Composable
fun LoginScreen(
    viewModel: LoginViewModel,
    onNavigateToRegister: () -> Unit,
    onContinueAsGuest: () -> Unit,
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
                onContinueAsGuest = onContinueAsGuest,
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
    onContinueAsGuest: () -> Unit,
) {
    val isActionInProgress = uiState.actionState is LoginUiState.ActionState.LoggingIn

    Column(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        Text(text = "Login", style = MaterialTheme.typography.headlineMedium)

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
            errorText = uiState.passwordError,
        )

        FormApiError(errorText = uiState.apiError)

        LoadingButton(
            onClick = onLoginClicked,
            enabled = uiState.isLoginEnabled,
            isLoading = isActionInProgress,
            modifier = Modifier.fillMaxWidth().height(48.dp),
        ) {
            Text("Login")
        }

        TextButton(onClick = onNavigateToRegister) {
            Text("Don't have an account? Register")
        }

        HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))

        LoadingOutlinedButton(
            onClick = onContinueAsGuest,
            modifier = Modifier.fillMaxWidth().height(48.dp),
            enabled = !isActionInProgress,
            isLoading = false,
        ) {
            Text("Continue as Guest")
        }
    }
}
