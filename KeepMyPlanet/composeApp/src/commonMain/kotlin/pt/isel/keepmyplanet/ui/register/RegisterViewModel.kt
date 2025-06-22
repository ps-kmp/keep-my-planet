package pt.isel.keepmyplanet.ui.register

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
import pt.isel.keepmyplanet.data.api.UserApi
import pt.isel.keepmyplanet.data.http.ApiException
import pt.isel.keepmyplanet.domain.user.Email
import pt.isel.keepmyplanet.domain.user.Name
import pt.isel.keepmyplanet.domain.user.Password
import pt.isel.keepmyplanet.dto.user.RegisterRequest
import pt.isel.keepmyplanet.ui.register.model.RegisterEvent
import pt.isel.keepmyplanet.ui.register.model.RegisterUiState

class RegisterViewModel(
    private val userApi: UserApi,
) : ViewModel() {
    private val _uiState = MutableStateFlow(RegisterUiState())
    val uiState: StateFlow<RegisterUiState> = _uiState.asStateFlow()

    private val _events = Channel<RegisterEvent>(Channel.BUFFERED)
    val events: Flow<RegisterEvent> = _events.receiveAsFlow()

    fun onUsernameChanged(username: String) {
        _uiState.update { it.copy(username = username, usernameError = null) }
    }

    fun onEmailChanged(email: String) {
        _uiState.update { it.copy(email = email, emailError = null) }
    }

    fun onPasswordChanged(password: String) {
        _uiState.update { it.copy(password = password, passwordError = null) }
    }

    fun onConfirmPasswordChanged(confirmPassword: String) {
        _uiState.update { it.copy(confirmPassword = confirmPassword, confirmPasswordError = null) }
    }

    fun onRegisterClicked() {
        if (!validateForm()) return

        val currentState = _uiState.value
        viewModelScope.launch {
            _uiState.update { it.copy(actionState = RegisterUiState.ActionState.Registering) }
            try {
                val request =
                    RegisterRequest(
                        name = currentState.username.trim(),
                        email = currentState.email.trim(),
                        password = currentState.password,
                    )
                val result = userApi.registerUser(request)

                result
                    .onSuccess {
                        _events.send(
                            RegisterEvent.ShowSnackbar("Registration successful! Please login."),
                        )
                        _events.send(RegisterEvent.NavigateToLogin)
                    }.onFailure { exception ->
                        handleError("Registration failed", exception)
                    }
            } catch (e: Exception) {
                handleError("An unexpected error occurred", e)
            } finally {
                _uiState.update { it.copy(actionState = RegisterUiState.ActionState.Idle) }
            }
        }
    }

    private fun validateForm(): Boolean {
        val formState = _uiState.value
        var stateWithErrors = formState

        stateWithErrors =
            try {
                Name(formState.username)
                stateWithErrors.copy(usernameError = null)
            } catch (e: IllegalArgumentException) {
                stateWithErrors.copy(usernameError = e.message)
            }

        stateWithErrors =
            try {
                Email(formState.email)
                stateWithErrors.copy(emailError = null)
            } catch (e: IllegalArgumentException) {
                stateWithErrors.copy(emailError = e.message)
            }

        stateWithErrors =
            try {
                Password(formState.password)
                stateWithErrors.copy(passwordError = null)
            } catch (e: IllegalArgumentException) {
                stateWithErrors.copy(passwordError = e.message)
            }

        stateWithErrors =
            if (formState.password != formState.confirmPassword) {
                stateWithErrors.copy(confirmPasswordError = "Passwords do not match")
            } else {
                stateWithErrors.copy(confirmPasswordError = null)
            }

        _uiState.value = stateWithErrors
        return !stateWithErrors.hasErrors
    }

    private suspend fun handleError(
        prefix: String,
        exception: Throwable,
    ) {
        val message =
            when (exception) {
                is ApiException -> exception.error.message
                else -> "$prefix: ${exception.message ?: "Registration failed"}"
            }
        _events.send(RegisterEvent.ShowSnackbar(message))
    }
}
