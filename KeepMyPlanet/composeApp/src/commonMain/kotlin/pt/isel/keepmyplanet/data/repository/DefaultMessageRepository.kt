package pt.isel.keepmyplanet.data.repository

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import pt.isel.keepmyplanet.data.api.ChatApi
import pt.isel.keepmyplanet.data.cache.MessageCacheRepository
import pt.isel.keepmyplanet.domain.common.Id
import pt.isel.keepmyplanet.domain.message.Message
import pt.isel.keepmyplanet.mapper.message.toMessage

class DefaultMessageRepository(
    private val chatApi: ChatApi,
    private val messageCache: MessageCacheRepository,
) {
    suspend fun getMessages(
        eventId: Id,
        afterPosition: Int? = null,
    ): Result<List<Message>> =
        runCatching {
            val cachedMessages = messageCache.getMessagesByEventId(eventId)
            val lastPositionInCache = cachedMessages.maxOfOrNull { it.chatPosition }
            val fetchAfterPosition = afterPosition ?: lastPositionInCache

            val networkResult = chatApi.getMessages(eventId.value, fetchAfterPosition)
            if (networkResult.isSuccess) {
                val newMessages = networkResult.getOrThrow().map { it.toMessage() }
                messageCache.insertMessages(newMessages)
                (cachedMessages + newMessages).distinctBy { it.id }.sortedBy { it.chatPosition }
            } else {
                cachedMessages.ifEmpty { throw networkResult.exceptionOrNull()!! }
            }
        }

    suspend fun sendMessage(
        eventId: Id,
        content: String,
    ): Result<Unit> = chatApi.sendMessage(eventId.value, content)

    fun listenToMessages(eventId: Id): Flow<Result<Message>> =
        chatApi
            .listenToMessages(eventId.value)
            .map { result -> result.map { it.toMessage() } }
            .onEach { result ->
                result.onSuccess { message -> messageCache.insertMessages(listOf(message)) }
            }
}
