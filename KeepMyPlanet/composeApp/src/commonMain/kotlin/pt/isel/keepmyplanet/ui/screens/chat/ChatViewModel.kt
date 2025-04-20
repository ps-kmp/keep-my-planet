package pt.isel.keepmyplanet.ui.screens.chat

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import pt.isel.keepmyplanet.data.service.ChatService
import pt.isel.keepmyplanet.dto.message.MessageResponse

data class ChatState(
    val messages: List<MessageResponse> = emptyList(),
    val currentMessage: String = "",
    val isLoading: Boolean = false,
    val error: String? = null,
)

class ChatViewModel(
    private val chatService: ChatService,
    private val coroutineScope: CoroutineScope = CoroutineScope(Dispatchers.Main),
) {
    private val _state = MutableStateFlow(ChatState())
    val state: StateFlow<ChatState> = _state.asStateFlow()

    init {
        observeMessages()
    }

    private fun observeMessages() {
        coroutineScope.launch {
            chatService.messages.collect { messages ->
                _state.update { it.copy(messages = messages) }
            }
        }
    }

    fun updateCurrentMessage(message: String) {
        _state.update { it.copy(currentMessage = message) }
    }

    fun sendMessage() {
        val message = state.value.currentMessage.trim()
        if (message.isBlank()) return

        coroutineScope.launch {
            _state.update { it.copy(isLoading = true, error = null) }

            chatService.sendMessage(message).fold(
                onSuccess = {
                    _state.update { it.copy(isLoading = false, currentMessage = "") }
                },
                onFailure = { error ->
                    _state.update {
                        it.copy(
                            isLoading = false,
                            error = error.message ?: "Erro ao enviar mensagem",
                        )
                    }
                },
            )
        }
    }

    fun disconnect() {
        chatService.stopPolling()
    }
}
