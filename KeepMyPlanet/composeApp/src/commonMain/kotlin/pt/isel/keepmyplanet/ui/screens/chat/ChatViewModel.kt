package pt.isel.keepmyplanet.ui.screens.chat

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import pt.isel.keepmyplanet.data.service.ChatService
import pt.isel.keepmyplanet.dto.message.MessageResponse

data class ChatState(
    val messages: List<MessageResponse> = emptyList(),
    val currentMessage: String = "",
    val isSending: Boolean = false,
    val error: String? = null,
    val isConnected: Boolean = true,
)

class ChatViewModel(
    private val chatService: ChatService,
    private val ioDispatcher: CoroutineDispatcher = Dispatchers.Main,
) : ViewModel() {
    private val _state = MutableStateFlow(ChatState())
    val state: StateFlow<ChatState> = _state.asStateFlow()

    init {
        observeMessages()
    }

    private fun observeMessages() {
        viewModelScope.launch {
            try {
                chatService.messages
                    .collect { messages ->
                        _state.update { it.copy(messages = messages) }
                    }
            } catch (e: Exception) {
                _state.update {
                    it.copy(error = "Connection error: ${e.message ?: "Unknown issue"}")
                }
            }
        }
    }

    fun updateCurrentMessage(message: String) {
        _state.update { it.copy(currentMessage = message, error = null) }
    }

    fun sendMessage() {
        val messageToSend = state.value.currentMessage.trim()
        if (messageToSend.isBlank() || state.value.isSending) return

        viewModelScope.launch {
            _state.update { it.copy(isSending = true, error = null) }

            val result =
                withContext(ioDispatcher) {
                    chatService.sendMessage(messageToSend)
                }

            result.fold(
                onSuccess = { _state.update { it.copy(isSending = false, currentMessage = "") } },
                onFailure = { error ->
                    _state.update {
                        it.copy(
                            isSending = false,
                            error = error.message ?: "Failed to send message",
                        )
                    }
                },
            )
        }
    }

    override fun onCleared() {
        super.onCleared()
        chatService.leaveChat()
    }
}
