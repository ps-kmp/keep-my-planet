package pt.isel.keepmyplanet.ui.chat

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import pt.isel.keepmyplanet.data.api.ChatApi
import pt.isel.keepmyplanet.dto.message.MessageResponse
import pt.isel.keepmyplanet.ui.chat.model.ChatEvent
import pt.isel.keepmyplanet.ui.chat.model.ChatInfo
import pt.isel.keepmyplanet.ui.chat.model.ChatUiState
import pt.isel.keepmyplanet.ui.user.model.UserInfo

class ChatViewModel(
    private val chatApi: ChatApi,
    user: UserInfo,
    chatInfo: ChatInfo,
) : ViewModel() {
    private val _uiState = MutableStateFlow(ChatUiState(user = user, chatInfo = chatInfo))
    val uiState: StateFlow<ChatUiState> = _uiState.asStateFlow()

    private val _events = Channel<ChatEvent>(Channel.BUFFERED)
    val events: Flow<ChatEvent> = _events.receiveAsFlow()

    init {
        loadInitialMessages()
        startListeningToMessages()
    }

    private fun loadInitialMessages() {
        _uiState.update { it.copy(isLoading = true) }
        viewModelScope.launch {
            val result = chatApi.getMessages(_uiState.value.chatInfo.eventId.value)

            result
                .onSuccess { messages ->
                    _uiState.update { it.copy(isLoading = false, messages = messages) }
                    if (messages.isNotEmpty()) _events.send(ChatEvent.ScrollToBottom)
                }.onFailure { exception ->
                    handleError("Failed to load messages", exception, showSnackbar = true)
                }
        }
    }

    private fun startListeningToMessages() {
        viewModelScope.launch {
            chatApi
                .listenToMessages(_uiState.value.chatInfo.eventId.value)
                .catch { exception ->
                    handleError("Chat connection error", exception, showSnackbar = true)
                }.collect { messageResult ->
                    messageResult
                        .onSuccess { newMessage ->
                            val messageAdded = addNewMessage(newMessage)
                            if (messageAdded) _events.send(ChatEvent.ScrollToBottom)
                        }.onFailure { exception ->
                            handleError("Error processing incoming message", exception, true)
                        }
                }
        }
    }

    private fun addNewMessage(newMessage: MessageResponse): Boolean {
        var wasAdded = false
        _uiState.update { currentState ->
            if (currentState.messages.any { it.id == newMessage.id }) {
                currentState
            } else {
                wasAdded = true
                currentState.copy(messages = currentState.messages + newMessage)
            }
        }
        return wasAdded
    }

    fun onMessageChanged(newMessage: String) {
        _uiState.update { it.copy(messageInput = newMessage) }
    }

    fun sendMessage() {
        val currentState = _uiState.value
        if (!currentState.isSendEnabled) return

        val messageContent = currentState.messageInput.trim()
        _uiState.update { it.copy(isSending = true, messageInput = "") }

        viewModelScope.launch {
            val result =
                chatApi.sendMessage(currentState.chatInfo.eventId.value, messageContent)

            result
                .onSuccess {
                    _uiState.update { it.copy(isSending = false) }
                }.onFailure { exception ->
                    _uiState.update { it.copy(isSending = false, messageInput = messageContent) }
                    handleError("Failed to send message", exception, showSnackbar = true)
                }
        }
    }

    private suspend fun handleError(
        prefix: String,
        exception: Throwable,
        showSnackbar: Boolean = false,
    ) {
        val errorMsg = "$prefix: ${exception.message ?: "Unknown error"}"
        _uiState.update { it.copy(isLoading = false, isSending = false) }
        if (showSnackbar) _events.send(ChatEvent.ShowSnackbar(errorMsg))
    }
}
