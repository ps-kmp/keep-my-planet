package pt.isel.keepmyplanet.data.repository

import kotlinx.datetime.LocalDateTime
import pt.isel.keepmyplanet.cache.KeepMyPlanetCache
import pt.isel.keepmyplanet.cache.MessageCache
import pt.isel.keepmyplanet.domain.common.Id
import pt.isel.keepmyplanet.domain.message.Message
import pt.isel.keepmyplanet.domain.message.MessageContent
import pt.isel.keepmyplanet.domain.user.Name

fun MessageCache.toMessage(): Message =
    Message(
        id = Id(this.id.toUInt()),
        eventId = Id(this.eventId.toUInt()),
        senderId = Id(this.senderId.toUInt()),
        senderName = Name(this.senderName),
        content = MessageContent(this.content),
        timestamp = LocalDateTime.parse(this.timestamp),
        chatPosition = this.chatPosition.toInt(),
    )

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
