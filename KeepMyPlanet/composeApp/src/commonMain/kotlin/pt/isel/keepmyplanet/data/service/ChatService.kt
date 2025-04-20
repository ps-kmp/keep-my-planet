package pt.isel.keepmyplanet.data.service

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import pt.isel.keepmyplanet.data.api.MessageClient
import pt.isel.keepmyplanet.data.api.createHttpClient
import pt.isel.keepmyplanet.data.model.UserSession
import pt.isel.keepmyplanet.dto.message.AddMessageRequest
import pt.isel.keepmyplanet.dto.message.MessageResponse

class ChatService(
    private val api: MessageClient = MessageClient(createHttpClient()),
    private val coroutineScope: CoroutineScope = CoroutineScope(Dispatchers.Default),
) {
    private val _messages = MutableStateFlow<List<MessageResponse>>(emptyList())
    val messages: StateFlow<List<MessageResponse>> = _messages.asStateFlow()

    private var userSession: UserSession? = null
    private var isPolling = false

    suspend fun joinEvent(
        username: String,
        eventName: String,
    ): Result<UserSession> =
        api.joinEvent(username, eventName).also { result ->
            result.onSuccess { session ->
                userSession = session
                startPolling()
            }
        }

    suspend fun sendMessage(content: String): Result<MessageResponse> {
        val session = userSession ?: return Result.failure(Exception("Utilizador não está em nenhum evento"))

        return api
            .sendMessage(
                eventId = session.eventId,
                message = AddMessageRequest(content),
            ).also { result ->
                result.onSuccess {
                    refreshMessages()
                }
            }
    }

    private fun startPolling() {
        if (isPolling) return

        isPolling = true
        coroutineScope.launch {
            while (isPolling) {
                refreshMessages()
                delay(3000) // Polling a cada 3 segundos
            }
        }
    }

    private suspend fun refreshMessages() {
        val session = userSession ?: return

        api.getEventMessages(session.eventId).onSuccess { newMessages ->
            _messages.value = newMessages
        }
    }

    fun stopPolling() {
        isPolling = false
        userSession = null
    }
}
