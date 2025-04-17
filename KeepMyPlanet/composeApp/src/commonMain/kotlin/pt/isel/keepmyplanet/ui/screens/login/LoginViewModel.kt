package pt.isel.keepmyplanet.ui.screens.login

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import pt.isel.keepmyplanet.data.model.UserSession
import pt.isel.keepmyplanet.data.service.ChatService

data class LoginState(
    val username: String = "",
    val eventName: String = "",
    val isLoading: Boolean = false,
    val error: String? = null,
)

class LoginViewModel(
    private val chatService: ChatService,
    private val coroutineScope: CoroutineScope = CoroutineScope(Dispatchers.Main),
) {
    private val _state = MutableStateFlow(LoginState())
    val state: StateFlow<LoginState> = _state.asStateFlow()

    fun updateUsername(username: String) {
        _state.update { it.copy(username = username) }
    }

    fun updateEventName(eventName: String) {
        _state.update { it.copy(eventName = eventName) }
    }

    fun login(onSuccess: (UserSession) -> Unit) {
        val username = state.value.username
        val eventName = state.value.eventName

        if (username.isBlank() || eventName.isBlank()) {
            _state.update { it.copy(error = "Username e nome do evento são obrigatórios") }
            return
        }

        coroutineScope.launch {
            _state.update { it.copy(isLoading = true, error = null) }

            chatService.joinEvent(username, eventName).fold(
                onSuccess = { session ->
                    _state.update { it.copy(isLoading = false) }
                    onSuccess(session)
                },
                onFailure = { error ->
                    _state.update {
                        it.copy(
                            isLoading = false,
                            error = error.message ?: "Erro ao entrar no evento",
                        )
                    }
                },
            )
        }
    }
}
