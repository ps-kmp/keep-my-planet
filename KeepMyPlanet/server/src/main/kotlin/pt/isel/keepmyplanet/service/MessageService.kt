package pt.isel.keepmyplanet.service

import pt.isel.keepmyplanet.domain.common.Id
import pt.isel.keepmyplanet.domain.event.EventStatus
import pt.isel.keepmyplanet.domain.message.Message
import pt.isel.keepmyplanet.domain.message.MessageContent
import pt.isel.keepmyplanet.errors.AuthorizationException
import pt.isel.keepmyplanet.errors.ConflictException
import pt.isel.keepmyplanet.errors.InternalServerException
import pt.isel.keepmyplanet.errors.NotFoundException
import pt.isel.keepmyplanet.errors.ValidationException
import pt.isel.keepmyplanet.repository.EventRepository
import pt.isel.keepmyplanet.repository.MessageRepository
import pt.isel.keepmyplanet.util.now

class MessageService(
    private val messageRepository: MessageRepository,
    private val eventRepository: EventRepository,
) {
    suspend fun getAllMessagesFromEvent(eventId: Id): Result<List<Message>> =
        runCatching {
            eventRepository.getById(eventId)
                ?: throw NotFoundException("Event '$eventId' not found.")
            messageRepository.getAllByEventId(eventId)
        }

    suspend fun getSingleMessageBySequence(
        eventId: Id,
        sequenceNum: Int,
    ): Result<Message> =
        runCatching {
            if (sequenceNum < 0) throw ValidationException("Invalid sequence number.")

            eventRepository.getById(eventId)
                ?: throw NotFoundException("Event '$eventId' not found.")
            eventRepository.getById(eventId)
                ?: throw NotFoundException("Event '$eventId' not found.")
            messageRepository.getSingleByEventIdAndSeqNum(eventId, sequenceNum)
                ?: throw NotFoundException("Message $sequenceNum in event '$eventId' not found.")
        }

    suspend fun addMessage(
        eventId: Id,
        senderId: Id,
        content: String,
    ): Result<Message> =
        runCatching {
            val event =
                eventRepository.getById(eventId)
                    ?: throw NotFoundException("Event '$eventId' not found.")

            if (event.status == EventStatus.CANCELLED || event.status == EventStatus.COMPLETED) {
                throw ConflictException("Cannot add messages to a ${event.status} event")
            }

            if (senderId != event.organizerId && senderId !in event.participantsIds) {
                throw AuthorizationException(
                    "User '$senderId' must be the organizer or a participant to send messages.",
                )
            }

            val messageContent = MessageContent(content)
            val newMessage =
                Message(
                    id = Id(1U),
                    eventId = eventId,
                    senderId = senderId,
                    content = messageContent,
                    timestamp = now(),
                    chatPosition = -1,
                )

            messageRepository.create(newMessage)
        }

    suspend fun deleteMessage(
        eventId: Id,
        sequenceNum: Int,
        requestingUserId: Id,
    ): Result<Unit> =
        runCatching {
            if (sequenceNum < 0) throw ValidationException("Invalid sequence number.")

            val event =
                eventRepository.getById(eventId)
                    ?: throw NotFoundException("Event '$eventId' not found.")
            val message =
                messageRepository.getSingleByEventIdAndSeqNum(eventId, sequenceNum)
                    ?: throw NotFoundException("Message $sequenceNum in event '$eventId' not found.")

            if (message.senderId != requestingUserId && event.organizerId != requestingUserId) {
                throw AuthorizationException(
                    "User $requestingUserId cannot delete message '${message.id.value}'",
                )
            }

            val deleted = messageRepository.deleteById(message.id)
            if (!deleted) {
                throw InternalServerException(
                    "Failed to delete message with sequence number $sequenceNum in event $eventId.",
                )
            }
        }
}
