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

    companion object {
        const val MAX_MESSAGE_LENGTH = 1000
    }

    init {
        loadMessages()
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
        _uiState.update {
            it.copy(
                actionState = ChatUiState.ActionState.Sending,
                messageInput = "",
                messageInputError = null,
            )
        }

        viewModelScope.launch {
            val result =
                chatApi.sendMessage(currentState.chatInfo.eventId.value, messageContent)

            result
                .onSuccess {
                    _uiState.update { it.copy(actionState = ChatUiState.ActionState.Idle) }
                }.onFailure { exception ->
                    _uiState.update {
                        it.copy(
                            actionState = ChatUiState.ActionState.Idle,
                            messageInput = messageContent,
                        )
                    }
                    handleError("Failed to send message", exception)
                }
        }
    }

    fun loadMessages() {
        _uiState.update { it.copy(isLoading = true, error = null) }
        viewModelScope.launch {
            val result = chatApi.getMessages(_uiState.value.chatInfo.eventId.value)

            result
                .onSuccess { messages ->
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            messages = messages.map { dto -> dto.toMessage() }.reversed(),
                        )
                    }
                    if (messages.isNotEmpty()) _events.send(ChatEvent.ScrollToBottom)
                }.onFailure { exception ->
                    val errorMsg = getErrorMessage("Failed to load messages", exception)
                    _uiState.update { it.copy(isLoading = false, error = errorMsg) }
                }
        }
    }

    private fun validateMessage(content: String): String? =
        try {
            if (content.length > MAX_MESSAGE_LENGTH) {
                "Message exceeds maximum length of $MAX_MESSAGE_LENGTH characters."
            } else if (content.isNotBlank()) {
                MessageContent(content)
                null
            } else {
                null
            }
        } catch (e: IllegalArgumentException) {
            e.message
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
                currentState.copy(messages = listOf(newMessage) + currentState.messages)
            }
        }
        return wasAdded
    }

    private fun getErrorMessage(
        prefix: String,
        exception: Throwable,
    ): String =
        when (exception) {
            is ApiException -> exception.error.message
            else -> "$prefix: ${exception.message ?: "Unknown error"}"
        }

    private suspend fun handleError(
        prefix: String,
        exception: Throwable,
    ) {
        val errorMsg = getErrorMessage(prefix, exception)
        _uiState.update { it.copy(isLoading = false, actionState = ChatUiState.ActionState.Idle) }
        _events.send(ChatEvent.ShowSnackbar(errorMsg))
    }
}
