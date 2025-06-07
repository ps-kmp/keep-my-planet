package pt.isel.keepmyplanet.ui.screens.login

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
import pt.isel.keepmyplanet.data.model.toUserSession
import pt.isel.keepmyplanet.data.service.AuthHttpClient
import pt.isel.keepmyplanet.dto.auth.LoginRequest

class LoginViewModel(
    private val authHttpClient: AuthHttpClient,
) : ViewModel() {
    private val _uiState = MutableStateFlow(LoginUiState())
    val uiState: StateFlow<LoginUiState> = _uiState.asStateFlow()

    private val _events = Channel<LoginEvent>(Channel.BUFFERED)
    val events: Flow<LoginEvent> = _events.receiveAsFlow()

    fun onUsernameChanged(username: String) {
        _uiState.update { it.copy(username = username) }
    }

    fun onPasswordChanged(password: String) {
        _uiState.update { it.copy(password = password) }
    }

    fun onLoginClicked() {
        val currentState = _uiState.value
        if (!currentState.isLoginEnabled) return

        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            try {
                val request = LoginRequest(currentState.username.trim(), currentState.password)
                val result = authHttpClient.login(request)

                result
                    .onSuccess { loginResponse ->
                        _events.send(LoginEvent.NavigateToHome(loginResponse.toUserSession()))
                    }.onFailure { exception ->
                        val errorMessage = exception.message ?: "An unknown error occurred"
                        _events.send(LoginEvent.ShowSnackbar(errorMessage))
                    }
            } catch (e: Exception) {
                _events.send(LoginEvent.ShowSnackbar(e.message ?: "An unexpected error occurred"))
            } finally {
                _uiState.update { it.copy(isLoading = false) }
            }
        }
    }
}
