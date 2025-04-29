package pt.isel.keepmyplanet.ui.screens.chat

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
import pt.isel.keepmyplanet.data.model.EventInfo
import pt.isel.keepmyplanet.data.model.UserInfo
import pt.isel.keepmyplanet.data.service.ChatService
import pt.isel.keepmyplanet.dto.message.MessageResponse

class ChatViewModel(
    private val chatService: ChatService,
    val user: UserInfo,
    val event: EventInfo,
) : ViewModel() {
    private val _uiState = MutableStateFlow(ChatUiState(user = user, event = event))
    val uiState: StateFlow<ChatUiState> = _uiState.asStateFlow()

    private val _events = Channel<ChatEvent>(Channel.BUFFERED)
    val events: Flow<ChatEvent> = _events.receiveAsFlow()

    init {
        loadInitialMessages()
        startListeningToMessages()
    }

    private fun mapToChatMessageUi(response: MessageResponse): ChatMessageUi =
        ChatMessageUi(
            id = response.id,
            senderId = response.senderId,
            content = response.content,
            timestamp = response.timestamp,
            isCurrentUser = response.senderId == user.id,
        )

    fun loadInitialMessages() {
        _uiState.update { it.copy(isLoading = true, errorMessage = null) }
        viewModelScope.launch {
            val result = chatService.getMessages(event.id)

            result
                .onSuccess { messages ->
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            messages = messages.map { message -> mapToChatMessageUi(message) },
                        )
                    }
                    if (messages.isNotEmpty()) _events.send(ChatEvent.ScrollToBottom)
                }.onFailure { exception ->
                    handleError("Failed to load messages", exception)
                }
        }
    }

    private fun startListeningToMessages() {
        viewModelScope.launch {
            chatService
                .listenToMessages(event.id)
                .catch { exception ->
                    handleError("Chat connection error", exception)
                }.collect { messageResult ->
                    messageResult
                        .onSuccess { newMessage ->
                            addNewMessage(mapToChatMessageUi(newMessage))
                            _events.send(ChatEvent.ScrollToBottom)
                        }.onFailure { exception ->
                            handleError("Error processing incoming message", exception, showSnackbar = true)
                        }
                }
        }
    }

    private fun addNewMessage(newMessage: ChatMessageUi) {
        _uiState.update { currentState ->
            if (currentState.messages.any { it.id == newMessage.id }) {
                currentState
            } else {
                currentState.copy(messages = currentState.messages + newMessage)
            }
        }
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
            val result = chatService.sendMessage(event.id, user.id, messageContent)

            result
                .onSuccess {
                    _uiState.update { it.copy(isSending = false) }
                }.onFailure { exception ->
                    handleError("Failed to send message", exception, showSnackbar = true)
                    _uiState.update { it.copy(isSending = false, messageInput = messageContent) }
                }
        }
    }

    private suspend fun handleError(
        prefix: String,
        exception: Throwable,
        showSnackbar: Boolean = false,
    ) {
        val errorMsg = "$prefix: ${exception.message ?: "Unknown error"}"
        _uiState.update { it.copy(isLoading = false, isSending = false, errorMessage = errorMsg) }
        if (showSnackbar) _events.send(ChatEvent.ShowSnackbar(errorMsg))
    }
}
