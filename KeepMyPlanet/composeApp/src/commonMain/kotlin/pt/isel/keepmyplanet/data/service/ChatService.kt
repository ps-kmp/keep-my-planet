package pt.isel.keepmyplanet.data.service

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import pt.isel.keepmyplanet.data.api.MessageClient
import pt.isel.keepmyplanet.data.model.UserSession
import pt.isel.keepmyplanet.dto.message.AddMessageRequest
import pt.isel.keepmyplanet.dto.message.MessageResponse

class ChatService(
    private val api: MessageClient,
    private val coroutineScope: CoroutineScope = CoroutineScope(Dispatchers.Default),
) {
    private val _messages = MutableStateFlow<List<MessageResponse>>(emptyList())
    val messages: StateFlow<List<MessageResponse>> = _messages.asStateFlow()

    private var userSession: UserSession? = null
    private var sseJob: Job? = null

    suspend fun joinEvent(
        username: String,
        eventName: String,
    ): Result<UserSession> =
        api.joinEvent(username, eventName).onSuccess { session ->
            userSession = session
            startSse(session.eventId)
        }

    suspend fun sendMessage(content: String): Result<MessageResponse> {
        val session = userSession ?: return Result.failure(Exception("User not in any event."))
        return api.sendMessage(session.eventId, AddMessageRequest(content))
    }

    private fun startSse(eventId: UInt) {
        if (sseJob?.isActive == true) return

        sseJob =
            api.startSse(
                eventId = eventId,
                scope = coroutineScope,
                onMessage = { msg -> _messages.update { it + msg } },
            )
    }

    fun stopSse() {
        sseJob?.cancel()
        sseJob = null
        userSession = null
        _messages.value = emptyList()
    }
}
