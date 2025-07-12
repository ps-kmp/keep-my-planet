package pt.isel.keepmyplanet.data.repository

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import pt.isel.keepmyplanet.data.api.ChatApi
import pt.isel.keepmyplanet.data.cache.MessageCacheRepository
import pt.isel.keepmyplanet.domain.common.Id
import pt.isel.keepmyplanet.domain.message.Message
import pt.isel.keepmyplanet.mapper.message.toMessage

class MessageApiRepository(
    private val chatApi: ChatApi,
    private val messageCache: MessageCacheRepository?,
) {
    suspend fun getMessages(
        eventId: Id,
        beforePosition: Int? = null,
        limit: Int? = null,
    ): Result<List<Message>> =
        runCatching {
            val networkResult = chatApi.getMessages(eventId.value, beforePosition, limit)
            if (networkResult.isSuccess) {
                val newMessages = networkResult.getOrThrow().map { it.toMessage() }
                messageCache?.insertMessages(newMessages)
                newMessages
            } else {
                if (beforePosition == null) {
                    messageCache?.getMessagesByEventId(eventId)
                        ?: throw networkResult.exceptionOrNull()!!
                } else {
                    throw networkResult.exceptionOrNull()!!
                }
            }
        }

    suspend fun sendMessage(
        eventId: Id,
        content: String,
    ): Result<Unit> = chatApi.sendMessage(eventId.value, content)

    fun listenToMessages(
        eventId: Id,
        token: String,
    ): Flow<Result<Message>> =
        chatApi
            .listenToMessages(eventId.value, token)
            .map { result -> result.map { it.toMessage() } }
            .onEach { result ->
                result.onSuccess { message -> messageCache?.insertMessages(listOf(message)) }
            }
}
