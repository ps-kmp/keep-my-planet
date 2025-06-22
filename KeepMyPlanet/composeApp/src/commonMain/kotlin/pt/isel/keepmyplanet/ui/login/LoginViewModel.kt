package pt.isel.keepmyplanet.ui.login

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import pt.isel.keepmyplanet.data.api.AuthApi
import pt.isel.keepmyplanet.data.http.ApiException
import pt.isel.keepmyplanet.data.mapper.toUserSession
import pt.isel.keepmyplanet.domain.user.Email
import pt.isel.keepmyplanet.dto.auth.LoginRequest
import pt.isel.keepmyplanet.ui.login.model.LoginEvent
import pt.isel.keepmyplanet.ui.login.model.LoginUiState

class LoginViewModel(
    private val authApi: AuthApi,
) : ViewModel() {
    private val _uiState = MutableStateFlow(LoginUiState())
    val uiState: StateFlow<LoginUiState> = _uiState.asStateFlow()

    private val _events = Channel<LoginEvent>(Channel.BUFFERED)
    val events: Flow<LoginEvent> = _events.receiveAsFlow()

    fun onEmailChanged(email: String) {
        _uiState.update { it.copy(email = email, emailError = null) }
    }

    fun onPasswordChanged(password: String) {
        _uiState.update { it.copy(password = password) }
    }

    fun onLoginClicked() {
        if (!validateForm()) return

        val currentState = _uiState.value
        if (!currentState.isLoginEnabled) return

        viewModelScope.launch {
            _uiState.update { it.copy(actionState = LoginUiState.ActionState.LoggingIn) }
            try {
                val request = LoginRequest(currentState.email.trim(), currentState.password)
                val result = authApi.login(request)

                result
                    .onSuccess { loginResponse ->
                        _events.send(LoginEvent.LoginSuccess(loginResponse.toUserSession()))
                    }.onFailure { exception ->
                        handleError("Login failed", exception)
                    }
            } catch (e: Exception) {
                handleError("An unexpected error occurred", e)
            } finally {
                _uiState.update { it.copy(actionState = LoginUiState.ActionState.Idle) }
            }
        }
    }

    private fun validateForm(): Boolean {
        val formState = _uiState.value
        val emailError =
            try {
                Email(formState.email)
                null
            } catch (e: IllegalArgumentException) {
                e.message
            }

        _uiState.update { it.copy(emailError = emailError) }
        return !_uiState.value.hasError
    }

    private suspend fun handleError(
        prefix: String,
        exception: Throwable,
    ) {
        val message =
            when (exception) {
                is ApiException -> exception.error.message
                else -> "$prefix: ${exception.message ?: "An unknown error occurred"}"
            }
        _events.send(LoginEvent.ShowSnackbar(message))
    }
}
