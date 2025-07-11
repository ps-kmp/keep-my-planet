package pt.isel.keepmyplanet.ui.chat

import kotlin.random.Random
import kotlin.random.nextUInt
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.launch
import pt.isel.keepmyplanet.data.repository.EventApiRepository
import pt.isel.keepmyplanet.data.repository.MessageApiRepository
import pt.isel.keepmyplanet.domain.common.Id
import pt.isel.keepmyplanet.domain.message.ChatInfo
import pt.isel.keepmyplanet.domain.message.Message
import pt.isel.keepmyplanet.domain.message.MessageContent
import pt.isel.keepmyplanet.session.SessionManager
import pt.isel.keepmyplanet.ui.base.BaseViewModel
import pt.isel.keepmyplanet.ui.chat.states.ChatEvent
import pt.isel.keepmyplanet.ui.chat.states.ChatUiState
import pt.isel.keepmyplanet.ui.chat.states.SendStatus
import pt.isel.keepmyplanet.ui.chat.states.UiMessage
import pt.isel.keepmyplanet.utils.now

class ChatViewModel(
    private val messageRepository: MessageApiRepository,
    private val eventRepository: EventApiRepository,
    sessionManager: SessionManager,
) : BaseViewModel<ChatUiState>(
        ChatUiState(sessionManager.userSession.value?.userInfo, ChatInfo(Id(0U), null)),
    ) {
    companion object {
        const val MAX_MESSAGE_LENGTH = 1000
        private const val PAGE_SIZE = 20
    }

    fun load(chatInfo: ChatInfo) {
        setState {
            copy(
                chatInfo = chatInfo,
                messages = emptyList(),
                error = null,
                isLoading = true,
                hasMoreMessages = true,
            )
        }
        if (currentState.user == null) {
            setState {
                copy(
                    error = "User is not logged in. Cannot display chat.",
                    isLoading = false,
                )
            }
        } else {
            if (chatInfo.eventTitle == null) {
                fetchEventDetails(chatInfo.eventId)
            }
            loadInitialMessages(chatInfo.eventId)
            startListeningToMessages(chatInfo.eventId)
        }
    }

    private fun fetchEventDetails(eventId: Id) {
        viewModelScope.launch {
            eventRepository
                .getEventDetails(eventId)
                .onSuccess { event ->
                    setState {
                        copy(
                            chatInfo = currentState.chatInfo.copy(eventTitle = event.title),
                        )
                    }
                }.onFailure {
                    handleErrorWithMessage("Could not load event title.")
                }
        }
    }

    override fun handleErrorWithMessage(message: String) {
        setState {
            copy(
                isLoading = false,
                isLoadingMore = false,
                actionState = ChatUiState.ActionState.Idle,
            )
        }
        sendEvent(ChatEvent.ShowSnackbar(message))
    }

    fun onMessageChanged(newMessage: String) {
        val error = validateMessage(newMessage)
        setState { copy(messageInput = newMessage, messageInputError = error) }
    }

    private fun performSend(
        messageContent: String,
        tempId: Id,
    ) {
        val eventId = currentState.chatInfo.eventId
        viewModelScope.launch {
            messageRepository
                .sendMessage(eventId, messageContent)
                .onSuccess {
                    // Message sent is confirmed by the SSE stream,
                    // which will replace the temporary message.
                }.onFailure { error ->
                    setState {
                        copy(
                            messages =
                                messages.map {
                                    if (it.temporaryId == tempId) {
                                        it.copy(status = SendStatus.FAILED)
                                    } else {
                                        it
                                    }
                                },
                        )
                    }
                    handleErrorWithMessage(getErrorMessage("Failed to send message", error))
                }
        }
    }

    fun sendMessage() {
        if (!currentState.isSendEnabled || currentState.user == null) return

        val messageContent = currentState.messageInput.trim()
        val tempId = Id(Random.nextUInt())

        val optimisticMessage =
            UiMessage(
                message =
                    Message(
                        id = Id(0u),
                        eventId = currentState.chatInfo.eventId,
                        senderId = currentState.user!!.id,
                        senderName = currentState.user!!.name,
                        content = MessageContent(messageContent),
                        timestamp = now(),
                        chatPosition =
                            (
                                currentState.messages
                                    .firstOrNull()
                                    ?.message
                                    ?.chatPosition ?: 0
                            ) + 1,
                    ),
                status = SendStatus.SENDING,
                temporaryId = tempId,
            )

        setState {
            copy(
                messages = listOf(optimisticMessage) + messages,
                messageInput = "",
                messageInputError = null,
            )
        }
        sendEvent(ChatEvent.ScrollToBottom)
        performSend(messageContent, tempId)
    }

    fun retrySendMessage(temporaryId: Id) {
        val failedMessage =
            currentState.messages.find {
                it.temporaryId == temporaryId && it.status == SendStatus.FAILED
            } ?: return

        val optimisticMessage = failedMessage.copy(status = SendStatus.SENDING)

        setState {
            copy(
                messages =
                    listOf(
                        optimisticMessage,
                    ) + messages.filterNot { it.temporaryId == temporaryId },
            )
        }
        sendEvent(ChatEvent.ScrollToBottom)
        performSend(failedMessage.message.content.value, temporaryId)
    }

    private fun loadInitialMessages(eventId: Id) {
        if (currentState.user == null) return

        setState { copy(isLoading = true, error = null) }

        viewModelScope.launch {
            messageRepository
                .getMessages(eventId, limit = PAGE_SIZE)
                .onSuccess { newMessages ->
                    setState {
                        copy(
                            isLoading = false,
                            messages =
                                newMessages
                                    .map {
                                        UiMessage(
                                            it,
                                            SendStatus.SENT,
                                        )
                                    }.reversed(),
                            error = null,
                            hasMoreMessages = newMessages.size == PAGE_SIZE,
                        )
                    }
                    sendEvent(ChatEvent.ScrollToBottom)
                }.onFailure { error ->
                    val errorMsg = getErrorMessage("Failed to load messages", error)
                    setState { copy(error = errorMsg, isLoading = false) }
                }
        }
    }

    fun loadPreviousMessages() {
        if (currentState.isLoadingMore ||
            !currentState.hasMoreMessages ||
            currentState.messages.isEmpty()
        ) {
            return
        }

        setState { copy(isLoadingMore = true) }
        val eventId = currentState.chatInfo.eventId
        val oldestPosition =
            currentState.messages
                .last()
                .message.chatPosition

        viewModelScope.launch {
            messageRepository
                .getMessages(eventId, beforePosition = oldestPosition, limit = PAGE_SIZE)
                .onSuccess { olderMessages ->
                    setState {
                        copy(
                            isLoadingMore = false,
                            messages =
                                messages +
                                    olderMessages.map { UiMessage(it, SendStatus.SENT) }.reversed(),
                            hasMoreMessages = olderMessages.size == PAGE_SIZE,
                        )
                    }
                }.onFailure { error ->
                    handleErrorWithMessage(getErrorMessage("Failed to load older messages", error))
                    setState { copy(isLoadingMore = false) }
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

    private fun startListeningToMessages(eventId: Id) {
        if (currentState.user == null) return

        viewModelScope.launch {
            messageRepository
                .listenToMessages(eventId)
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
            if (messages.any { it.message.id == newMessage.id }) {
                this
            } else {
                wasAdded = true
                val newUiMessage = UiMessage(newMessage, SendStatus.SENT)
                val filteredMessages = messages.filterNot { it.temporaryId != null }
                copy(messages = listOf(newUiMessage) + filteredMessages)
            }
        }
        return wasAdded
    }
}
