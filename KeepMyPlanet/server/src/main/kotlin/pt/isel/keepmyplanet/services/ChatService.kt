package pt.isel.keepmyplanet.services

import kotlinx.datetime.Clock
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import pt.isel.keepmyplanet.domain.common.Id
import pt.isel.keepmyplanet.domain.message.Message
import pt.isel.keepmyplanet.domain.message.MessageContent
import pt.isel.keepmyplanet.repository.MessageRepository

class ChatService(
    private val messageRepository: MessageRepository,
) {
    fun getMessages(eventId: Id): List<Message> = messageRepository.findByEventId(eventId)

    suspend fun addMessage(
        eventId: Id,
        senderId: String,
        content: MessageContent,
    ): Message {
        val newMessage =
            Message(
                id = Id((messageRepository.findByEventId(eventId).size + 1).toUInt()),
                eventId = eventId,
                senderId = Id(senderId.toUInt()),
                content = content,
                timestamp = Clock.System.now().toLocalDateTime(TimeZone.UTC),
            )
        messageRepository.create(newMessage)
        return newMessage
    }
}
