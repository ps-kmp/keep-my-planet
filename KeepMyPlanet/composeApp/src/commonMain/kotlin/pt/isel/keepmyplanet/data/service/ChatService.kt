package pt.isel.keepmyplanet.data.service

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import pt.isel.keepmyplanet.data.api.MessageClient
import pt.isel.keepmyplanet.data.model.UserSession
import pt.isel.keepmyplanet.dto.message.AddMessageRequest
import pt.isel.keepmyplanet.dto.message.MessageResponse
import pt.isel.keepmyplanet.errors.AuthenticationException

class ChatService(
    private val api: MessageClient,
    private val externalScope: CoroutineScope = CoroutineScope(Dispatchers.Default + SupervisorJob()),
) {
    private val _messages = MutableStateFlow<List<MessageResponse>>(emptyList())
    val messages: StateFlow<List<MessageResponse>> = _messages.asStateFlow()

    private val _serviceErrors = MutableSharedFlow<Throwable>()
    val serviceErrors: SharedFlow<Throwable> = _serviceErrors.asSharedFlow()

    private var userSession: UserSession? = null
    private var currentEventId: UInt? = null
    private var sseJob: Job? = null

    suspend fun joinEvent(
        username: String,
        eventName: String,
    ): Result<Pair<UserSession, UInt>> {
        stopSseInternal()
        _messages.value = emptyList()
        return api
            .joinEvent(username, eventName)
            .onSuccess { (session, eventId) ->
                userSession = session
                currentEventId = eventId
                externalScope.launch { fetchInitialMessages(eventId) }
                startSse(eventId)
            }.onFailure {
                currentEventId = null
            }
    }

    suspend fun sendMessage(content: String): Result<MessageResponse> {
        val session = userSession
        val eventId = currentEventId

        if (session == null || eventId == null) {
            return Result.failure(AuthenticationException("User not in any event."))
        }

        return api.sendMessage(eventId, session.userId, AddMessageRequest(content))
    }

    private fun startSse(eventId: UInt) {
        if (sseJob?.isActive == true && currentEventId == eventId) return

        stopSseInternal()
        currentEventId = eventId

        sseJob =
            api.startSse(
                eventId = eventId,
                scope = externalScope,
                onMessage = { msg ->
                    _messages.update { list ->
                        if (list.any { it.id == msg.id }) list else list + msg
                    }
                },
            )
    }

    private suspend fun fetchInitialMessages(eventId: UInt) {
        api
            .getEventMessages(eventId)
            .onSuccess { initialMessages ->
                _messages.update { currentList ->
                    (initialMessages + currentList).distinctBy { it.id }
                }
            }
    }

    private fun stopSseInternal() {
        if (sseJob?.isActive == true) sseJob?.cancel()
        sseJob = null
    }

    fun leaveChat() {
        stopSseInternal()
        userSession = null
        currentEventId = null
    }
}
