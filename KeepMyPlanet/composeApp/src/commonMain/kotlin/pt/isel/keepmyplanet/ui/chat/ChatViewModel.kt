package pt.isel.keepmyplanet.ui.chat

import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.launch
import pt.isel.keepmyplanet.data.repository.DefaultEventRepository
import pt.isel.keepmyplanet.data.repository.DefaultMessageRepository
import pt.isel.keepmyplanet.domain.message.ChatInfo
import pt.isel.keepmyplanet.domain.message.Message
import pt.isel.keepmyplanet.domain.message.MessageContent
import pt.isel.keepmyplanet.session.SessionManager
import pt.isel.keepmyplanet.ui.base.BaseViewModel
import pt.isel.keepmyplanet.ui.chat.states.ChatEvent
import pt.isel.keepmyplanet.ui.chat.states.ChatUiState

class ChatViewModel(
    private val messageRepository: DefaultMessageRepository,
    private val eventRepository: DefaultEventRepository,
    sessionManager: SessionManager,
    chatInfo: ChatInfo,
) : BaseViewModel<ChatUiState>(ChatUiState(sessionManager.userSession.value?.userInfo, chatInfo)) {
    companion object {
        const val MAX_MESSAGE_LENGTH = 1000
    }

    init {
        if (currentState.user == null) {
            setState { copy(error = "User is not logged in. Cannot display chat.") }
        } else {
            if (currentState.chatInfo.eventTitle == null) {
                fetchEventDetails()
            }
            loadMessages()
            startListeningToMessages()
        }
    }

    private fun fetchEventDetails() {
        viewModelScope.launch {
            eventRepository
                .getEventDetails(currentState.chatInfo.eventId)
                .onSuccess { event ->
                    setState { copy(chatInfo = chatInfo.copy(eventTitle = event.title)) }
                }.onFailure {
                    handleErrorWithMessage("Could not load event title.")
                }
        }
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
        val eventId = currentState.chatInfo.eventId

        launchWithResult(
            onStart = {
                copy(
                    actionState = ChatUiState.ActionState.Sending,
                    messageInput = "",
                    messageInputError = null,
                )
            },
            onFinally = { copy(actionState = ChatUiState.ActionState.Idle) },
            block = { messageRepository.sendMessage(eventId, messageContent) },
            onSuccess = { },
            onError = {
                setState { copy(messageInput = messageContent) }
                handleErrorWithMessage(getErrorMessage("Failed to send message", it))
            },
        )
    }

    fun loadMessages() {
        if (currentState.user == null) return

        setState { copy(isLoading = true, error = null) }

        viewModelScope.launch {
            messageRepository
                .getMessages(currentState.chatInfo.eventId)
                .onSuccess { newMessages ->
                    setState {
                        copy(
                            isLoading = false,
                            messages = newMessages.reversed(),
                            error = null,
                        )
                    }
                    sendEvent(ChatEvent.ScrollToBottom)
                }.onFailure { error ->
                    if (currentState.messages.isEmpty()) {
                        val errorMsg = getErrorMessage("Failed to load messages", error)
                        setState { copy(error = errorMsg, isLoading = false) }
                    } else {
                        handleErrorWithMessage(getErrorMessage("Failed to refresh messages", error))
                        setState { copy(isLoading = false) }
                    }
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
        if (currentState.user == null) return

        viewModelScope.launch {
            messageRepository
                .listenToMessages(currentState.chatInfo.eventId)
                .catch { handleErrorWithMessage(getErrorMessage("Chat connection error", it)) }
                .collect { messageResult ->
                    messageResult
                        .onSuccess { newMessageResponse ->
                            val messageAdded = addNewMessage(newMessageResponse)
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
