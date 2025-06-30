package pt.isel.keepmyplanet.ui.chat

import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.launch
import pt.isel.keepmyplanet.data.api.ChatApi
import pt.isel.keepmyplanet.data.repository.EventsRepository
import pt.isel.keepmyplanet.data.repository.MessageCacheRepository
import pt.isel.keepmyplanet.domain.message.ChatInfo
import pt.isel.keepmyplanet.domain.message.Message
import pt.isel.keepmyplanet.domain.message.MessageContent
import pt.isel.keepmyplanet.mapper.message.toMessage
import pt.isel.keepmyplanet.session.SessionManager
import pt.isel.keepmyplanet.ui.chat.states.ChatEvent
import pt.isel.keepmyplanet.ui.chat.states.ChatUiState
import pt.isel.keepmyplanet.ui.viewmodel.BaseViewModel

class ChatViewModel(
    private val chatApi: ChatApi,
    private val messageCacheRepository: MessageCacheRepository,
    private val eventsRepository: EventsRepository,
    sessionManager: SessionManager,
    chatInfo: ChatInfo,
) : BaseViewModel<ChatUiState>(
        ChatUiState(
            user = sessionManager.userSession.value?.userInfo,
            chatInfo = chatInfo,
        ),
    ) {
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
            eventsRepository
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
        if (currentState.user == null) return

        viewModelScope.launch {
            val cachedMessages =
                messageCacheRepository.getMessagesByEventId(
                    currentState.chatInfo.eventId,
                )
            if (cachedMessages.isNotEmpty()) {
                setState { copy(messages = cachedMessages.reversed(), isLoading = false) }
                sendEvent(ChatEvent.ScrollToBottom)
            } else {
                setState { copy(isLoading = true, error = null) }
            }

            val lastPosition = cachedMessages.maxOfOrNull { it.chatPosition }
            val result = chatApi.getMessages(currentState.chatInfo.eventId.value, lastPosition)

            result
                .onSuccess { newMessages ->
                    if (newMessages.isNotEmpty()) {
                        val newDomainMessages = newMessages.map { it.toMessage() }
                        messageCacheRepository.insertMessages(newDomainMessages)
                        val allMessages =
                            (cachedMessages + newDomainMessages).sortedBy { it.chatPosition }
                        setState {
                            copy(
                                isLoading = false,
                                messages = allMessages.reversed(),
                                error = null,
                            )
                        }
                        sendEvent(ChatEvent.ScrollToBottom)
                    } else {
                        setState { copy(isLoading = false) }
                    }
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
                viewModelScope.launch { messageCacheRepository.insertMessages(listOf(newMessage)) }
                wasAdded = true
                copy(messages = listOf(newMessage) + messages)
            }
        }
        return wasAdded
    }
}
