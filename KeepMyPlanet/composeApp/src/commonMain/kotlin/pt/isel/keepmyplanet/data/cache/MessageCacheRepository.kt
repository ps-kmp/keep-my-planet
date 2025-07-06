package pt.isel.keepmyplanet.data.cache

import kotlin.time.Duration.Companion.seconds
import pt.isel.keepmyplanet.cache.KeepMyPlanetCache
import pt.isel.keepmyplanet.data.cache.mappers.toMessage
import pt.isel.keepmyplanet.domain.common.Id
import pt.isel.keepmyplanet.domain.message.Message
import pt.isel.keepmyplanet.utils.minus
import pt.isel.keepmyplanet.utils.now

class MessageCacheRepository(
    database: KeepMyPlanetCache,
) : CleanableCache {
    private val queries = database.messageCacheQueries

    suspend fun insertMessages(messages: List<Message>) {
        queries.transaction {
            messages.forEach { message ->
                queries.insertMessage(
                    id = message.id.value.toLong(),
                    eventId = message.eventId.value.toLong(),
                    senderId = message.senderId.value.toLong(),
                    senderName = message.senderName.value,
                    content = message.content.value,
                    timestamp = message.timestamp.toString(),
                    chatPosition = message.chatPosition.toLong(),
                )
            }
        }
    }

    suspend fun getMessagesByEventId(eventId: Id): List<Message> =
        queries.getMessagesByEventId(eventId.value.toLong()).executeAsList().map { it.toMessage() }

    suspend fun clearAllMessages() {
        queries.clearAllMessages()
    }

    override suspend fun cleanupExpiredData(ttlSeconds: Long) {
        val expirationDateTime = now().minus(ttlSeconds.seconds).toString()
        queries.deleteExpiredMessages(expirationDateTime)
    }

    suspend fun clearMessagesForEvent(eventId: Id) {
        queries.clearMessagesForEvent(eventId.value.toLong())
    }
}
