package pt.isel.keepmyplanet.ui.screens.login

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import pt.isel.keepmyplanet.data.model.UserSession
import pt.isel.keepmyplanet.data.service.ChatService

data class LoginState(
    val username: String = "",
    val eventName: String = "",
    val isLoading: Boolean = false,
    val error: String? = null,
)

sealed interface LoginEvent {
    data class LoginSuccess(
        val session: UserSession,
        val eventId: UInt,
    ) : LoginEvent

    data class LoginFailure(
        val message: String,
    ) : LoginEvent
}

class LoginViewModel(
    private val chatService: ChatService,
    private val ioDispatcher: CoroutineDispatcher = Dispatchers.Main,
) : ViewModel() {
    private val _state = MutableStateFlow(LoginState())
    val state: StateFlow<LoginState> = _state.asStateFlow()

    private val _loginEvent = MutableSharedFlow<LoginEvent>()
    val loginEvent: SharedFlow<LoginEvent> = _loginEvent.asSharedFlow()

    fun updateUsername(username: String) {
        _state.update { it.copy(username = username.trimStart(), error = null) }
    }

    fun updateEventName(eventName: String) {
        _state.update { it.copy(eventName = eventName.trimStart(), error = null) }
    }

    fun login() {
        val currentState = state.value
        val username = currentState.username.trim()
        val eventName = currentState.eventName.trim()

        if (username.isBlank() || eventName.isBlank()) {
            _state.update {
                it.copy(
                    username = username,
                    eventName = eventName,
                    error = "Username and event name cannot be empty.",
                )
            }
        }

        _state.update {
            it.copy(
                username = username,
                eventName = eventName,
                isLoading = true,
                error = null,
            )
        }

        viewModelScope.launch {
            val result =
                withContext(ioDispatcher) {
                    chatService.joinEvent(username, eventName)
                }

            result.fold(
                onSuccess = { (session, eventId) ->
                    _loginEvent.emit(LoginEvent.LoginSuccess(session, eventId))
                    _state.update { it.copy(isLoading = false) }
                },
                onFailure = { error ->
                    val errorMessage = error.message ?: "An unknown error occurred during login"
                    _state.update { it.copy(isLoading = false, error = errorMessage) }
                    _loginEvent.emit(LoginEvent.LoginFailure(errorMessage))
                },
            )
        }
    }
}
