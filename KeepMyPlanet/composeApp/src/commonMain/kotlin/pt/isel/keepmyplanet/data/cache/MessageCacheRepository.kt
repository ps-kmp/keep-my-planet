package pt.isel.keepmyplanet.data.cache

import pt.isel.keepmyplanet.cache.KeepMyPlanetCache
import pt.isel.keepmyplanet.data.cache.mappers.toMessage
import pt.isel.keepmyplanet.domain.common.Id
import pt.isel.keepmyplanet.domain.message.Message

class MessageCacheRepository(
    database: KeepMyPlanetCache,
) {
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

    fun getMessagesByEventId(eventId: Id): List<Message> =
        queries.getMessagesByEventId(eventId.value.toLong()).executeAsList().map { it.toMessage() }

    suspend fun clearAllMessages() {
        queries.clearAllMessages()
    }

    suspend fun deleteExpiredMessages(expirationDateTime: String) {
        queries.deleteExpiredMessages(expirationDateTime)
    }

    suspend fun clearMessagesForEvent(eventId: Id) {
        queries.clearMessagesForEvent(eventId.value.toLong())
    }
}
