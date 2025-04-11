package pt.isel.keepmyplanet.services

import pt.isel.keepmyplanet.core.NotFoundException
import pt.isel.keepmyplanet.domain.common.Id
import pt.isel.keepmyplanet.domain.event.EventStatus
import pt.isel.keepmyplanet.domain.message.Message
import pt.isel.keepmyplanet.domain.message.MessageContent
import pt.isel.keepmyplanet.repository.EventRepository
import pt.isel.keepmyplanet.repository.MessageRepository
import pt.isel.keepmyplanet.util.now

class MessageService(
    private val messageRepository: MessageRepository,
    private val eventRepository: EventRepository,
) {
    suspend fun getMessages(eventId: Id): Result<List<Message>> =
        runCatching {
            eventRepository.getById(eventId) ?: throw NotFoundException("Event", eventId)
            messageRepository.findByEventId(eventId)
        }

    suspend fun getSingleMessage(
        eventId: Id,
        messageNum: Int,
    ): Result<Message> =
        runCatching {
            eventRepository.getById(eventId) ?: throw NotFoundException("Event", eventId)
            val messages = messageRepository.findByEventId(eventId)
            messages.find { it.chatPosition == messageNum }
                ?: throw NotFoundException("Message with position $messageNum in event", eventId)
        }

    suspend fun addMessage(
        eventId: Id,
        senderId: Id,
        content: String,
    ): Result<Message> =
        runCatching {
            val event =
                eventRepository.getById(eventId)
                    ?: throw NotFoundException("Event", eventId)

            if (event.status == EventStatus.CANCELLED || event.status == EventStatus.COMPLETED) {
                throw IllegalStateException("Cannot add messages to a ${event.status} event")
            }

            if (senderId != event.organizerId && senderId !in event.participantsIds) {
                throw IllegalArgumentException("User $senderId is not a participant in this event")
            }
            val newMessage =
                Message(
                    chatPosition = 0,
                    eventId = eventId,
                    senderId = senderId,
                    content = MessageContent(content),
                    timestamp = now(),
                )

            messageRepository.create(newMessage)
        }
}
