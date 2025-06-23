package pt.isel.keepmyplanet.ui.chat

import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.launch
import pt.isel.keepmyplanet.data.api.ChatApi
import pt.isel.keepmyplanet.domain.message.Message
import pt.isel.keepmyplanet.domain.message.MessageContent
import pt.isel.keepmyplanet.dto.message.ChatInfo
import pt.isel.keepmyplanet.dto.user.UserInfo
import pt.isel.keepmyplanet.mapper.message.toMessage
import pt.isel.keepmyplanet.ui.base.BaseViewModel
import pt.isel.keepmyplanet.ui.chat.model.ChatEvent
import pt.isel.keepmyplanet.ui.chat.model.ChatUiState

class ChatViewModel(
    private val chatApi: ChatApi,
    user: UserInfo,
    chatInfo: ChatInfo,
) : BaseViewModel<ChatUiState>(ChatUiState(user, chatInfo)) {
    companion object {
        const val MAX_MESSAGE_LENGTH = 1000
    }

    init {
        loadMessages()
        startListeningToMessages()
    }

    override fun handleErrorWithMessage(message: String) {
        setState { copy(isLoading = false, actionState = ChatUiState.ActionState.Idle) }
        sendEvent(ChatEvent.ShowSnackbar(message))
    }

    fun onMessageChanged(newMessage: String) {
        val error = validateMessage(newMessage)
        setState { copy(messageInput = newMessage, messageInputError = error) }
    }

    fun sendMessage() {
        if (!currentState.isSendEnabled) return

        val messageContent = currentState.messageInput.trim()
        val eventId = currentState.chatInfo.eventId.value

        launchWithResult(
            onStart = {
                copy(
                    actionState = ChatUiState.ActionState.Sending,
                    messageInput = "",
                    messageInputError = null,
                )
            },
            onFinally = { copy(actionState = ChatUiState.ActionState.Idle) },
            block = { chatApi.sendMessage(eventId, messageContent) },
            onSuccess = { },
            onError = {
                setState { copy(messageInput = messageContent) }
                handleErrorWithMessage(getErrorMessage("Failed to send message", it))
            },
        )
    }

    fun loadMessages() {
        launchWithResult(
            onStart = { copy(isLoading = true, error = null) },
            onFinally = { copy(isLoading = false) },
            block = { chatApi.getMessages(currentState.chatInfo.eventId.value) },
            onSuccess = { messages ->
                setState { copy(messages = messages.map { dto -> dto.toMessage() }.reversed()) }
                if (messages.isNotEmpty()) sendEvent(ChatEvent.ScrollToBottom)
            },
            onError = {
                val errorMsg = getErrorMessage("Failed to load messages", it)
                setState { copy(error = errorMsg) }
            },
        )
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
                .listenToMessages(currentState.chatInfo.eventId.value)
                .catch { handleErrorWithMessage(getErrorMessage("Chat connection error", it)) }
                .collect { messageResult ->
                    messageResult
                        .onSuccess { newMessageResponse ->
                            val messageAdded = addNewMessage(newMessageResponse.toMessage())
                            if (messageAdded) sendEvent(ChatEvent.ScrollToBottom)
                        }.onFailure { exception ->
                            handleErrorWithMessage(
                                getErrorMessage("Error processing incoming message", exception),
                            )
                        }
                }
        }
    }

    private fun addNewMessage(newMessage: Message): Boolean {
        var wasAdded = false
        setState {
            if (messages.any { it.id == newMessage.id }) {
                this
            } else {
                wasAdded = true
                copy(messages = listOf(newMessage) + messages)
            }
        }
        return wasAdded
    }
}
