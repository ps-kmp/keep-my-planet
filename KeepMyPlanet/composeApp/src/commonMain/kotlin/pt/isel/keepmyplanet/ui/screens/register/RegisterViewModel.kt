package pt.isel.keepmyplanet.ui.screens.register

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
import pt.isel.keepmyplanet.data.service.UserService
import pt.isel.keepmyplanet.dto.user.RegisterRequest

class RegisterViewModel(
    private val userService: UserService,
) : ViewModel() {
    private val _uiState = MutableStateFlow(RegisterUiState())
    val uiState: StateFlow<RegisterUiState> = _uiState.asStateFlow()

    private val _events = Channel<RegisterEvent>(Channel.BUFFERED)
    val events: Flow<RegisterEvent> = _events.receiveAsFlow()

    fun onUsernameChanged(username: String) {
        _uiState.update { it.copy(username = username) }
    }

    fun onEmailChanged(email: String) {
        _uiState.update { it.copy(email = email) }
    }

    fun onPasswordChanged(password: String) {
        _uiState.update { it.copy(password = password) }
    }

    fun onConfirmPasswordChanged(confirmPassword: String) {
        _uiState.update { it.copy(confirmPassword = confirmPassword) }
    }

    fun onRegisterClicked() {
        val currentState = _uiState.value
        if (!currentState.isRegisterEnabled) return

        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            try {
                val request =
                    RegisterRequest(
                        name = currentState.username.trim(),
                        email = currentState.email.trim(),
                        password = currentState.password,
                    )
                val result = userService.registerUser(request)

                result
                    .onSuccess {
                        _events.send(RegisterEvent.ShowSnackbar("Registration successful! Please login."))
                        _events.send(RegisterEvent.NavigateToLogin)
                    }.onFailure { exception ->
                        val errorMessage = exception.message ?: "Registration failed"
                        _events.send(RegisterEvent.ShowSnackbar(errorMessage))
                    }
            } catch (e: Exception) {
                val errorMessage = e.message ?: "An unexpected error occurred during registration"
                _events.send(RegisterEvent.ShowSnackbar(errorMessage))
            } finally {
                _uiState.update { it.copy(isLoading = false) }
            }
        }
    }
}
