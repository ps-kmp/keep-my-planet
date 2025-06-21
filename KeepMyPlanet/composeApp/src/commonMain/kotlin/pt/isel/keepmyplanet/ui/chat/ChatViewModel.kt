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
import pt.isel.keepmyplanet.data.http.ApiException
import pt.isel.keepmyplanet.domain.message.Message
import pt.isel.keepmyplanet.domain.message.MessageContent
import pt.isel.keepmyplanet.mapper.message.toMessage
import pt.isel.keepmyplanet.ui.chat.model.ChatEvent
import pt.isel.keepmyplanet.ui.chat.model.ChatInfo
import pt.isel.keepmyplanet.ui.chat.model.ChatUiState
import pt.isel.keepmyplanet.ui.user.profile.model.UserInfo

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

    fun onMessageChanged(newMessage: String) {
        val error = validateMessage(newMessage)
        _uiState.update { it.copy(messageInput = newMessage, messageInputError = error) }
    }

    fun sendMessage() {
        val currentState = _uiState.value
        if (!currentState.isSendEnabled) return

        val messageContent = currentState.messageInput.trim()
        _uiState.update { it.copy(isSending = true, messageInput = "", messageInputError = null) }

        viewModelScope.launch {
            val result =
                chatApi.sendMessage(currentState.chatInfo.eventId.value, messageContent)

            result
                .onSuccess {
                    _uiState.update { it.copy(isSending = false) }
                }.onFailure { exception ->
                    _uiState.update { it.copy(isSending = false, messageInput = messageContent) }
                    handleError("Failed to send message", exception)
                }
        }
    }

    private fun validateMessage(content: String): String? =
        try {
            if (content.isNotBlank()) {
                MessageContent(content)
            }
            null
        } catch (e: IllegalArgumentException) {
            e.message
        }

    private fun loadInitialMessages() {
        _uiState.update { it.copy(isLoading = true) }
        viewModelScope.launch {
            val result = chatApi.getMessages(_uiState.value.chatInfo.eventId.value)

            result
                .onSuccess { messages ->
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            messages = messages.map { dto -> dto.toMessage() },
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
            chatApi
                .listenToMessages(_uiState.value.chatInfo.eventId.value)
                .catch { exception ->
                    handleError("Chat connection error", exception)
                }.collect { messageResult ->
                    messageResult
                        .onSuccess { newMessageResponse ->
                            val messageAdded = addNewMessage(newMessageResponse.toMessage())
                            if (messageAdded) _events.send(ChatEvent.ScrollToBottom)
                        }.onFailure { exception ->
                            handleError("Error processing incoming message", exception)
                        }
                }
        }
    }

    private fun addNewMessage(newMessage: Message): Boolean {
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

    private suspend fun handleError(
        prefix: String,
        exception: Throwable,
    ) {
        val errorMsg =
            when (exception) {
                is ApiException -> exception.error.message
                else -> "$prefix: ${exception.message ?: "Unknown error"}"
            }
        _uiState.update { it.copy(isLoading = false, isSending = false) }
        _events.send(ChatEvent.ShowSnackbar(errorMsg))
    }
}
