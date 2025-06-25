package pt.isel.keepmyplanet.service

import pt.isel.keepmyplanet.domain.common.Id
import pt.isel.keepmyplanet.domain.event.Event
import pt.isel.keepmyplanet.domain.event.EventStatus
import pt.isel.keepmyplanet.domain.message.Message
import pt.isel.keepmyplanet.domain.message.MessageContent
import pt.isel.keepmyplanet.exception.AuthorizationException
import pt.isel.keepmyplanet.exception.ConflictException
import pt.isel.keepmyplanet.exception.InternalServerException
import pt.isel.keepmyplanet.exception.NotFoundException
import pt.isel.keepmyplanet.exception.ValidationException
import pt.isel.keepmyplanet.repository.EventRepository
import pt.isel.keepmyplanet.repository.MessageRepository
import pt.isel.keepmyplanet.repository.UserRepository
import pt.isel.keepmyplanet.utils.now

class MessageService(
    private val messageRepository: MessageRepository,
    private val eventRepository: EventRepository,
    private val userRepository: UserRepository,
    private val chatSseService: ChatSseService,
) {
    suspend fun getAllMessagesFromEvent(eventId: Id): Result<List<Message>> =
        runCatching {
            findEventOrFail(eventId)
            messageRepository.getAllByEventId(eventId)
        }

    suspend fun getSingleMessageBySequence(
        eventId: Id,
        sequenceNum: Int,
    ): Result<Message> =
        runCatching {
            if (sequenceNum < 0) throw ValidationException("Invalid sequence number.")

            findEventOrFail(eventId)
            messageRepository.getSingleByEventIdAndSeqNum(eventId, sequenceNum)
                ?: throw NotFoundException("Message $sequenceNum in event '$eventId' not found.")
        }

    suspend fun addMessage(
        eventId: Id,
        senderId: Id,
        content: String,
    ): Result<Message> =
        runCatching {
            val event = findEventOrFail(eventId)

            if (event.status == EventStatus.CANCELLED || event.status == EventStatus.COMPLETED) {
                throw ConflictException("Cannot add messages to a ${event.status} event")
            }

            if (senderId != event.organizerId && senderId !in event.participantsIds) {
                throw AuthorizationException(
                    "User '$senderId' must be the organizer or a participant to send messages.",
                )
            }

            val senderName =
                userRepository.getById(senderId)
                    ?: throw NotFoundException(
                        "Could not find user details for sender '${senderId.value}'.",
                    )

            val messageContent = MessageContent(content)
            val newMessage =
                Message(
                    id = Id(0U),
                    eventId = eventId,
                    senderId = senderId,
                    senderName = senderName.name,
                    content = messageContent,
                    timestamp = now(),
                    chatPosition = -1,
                )

            val createdMessage = messageRepository.create(newMessage)
            chatSseService.publish(createdMessage)
            createdMessage
        }

    suspend fun deleteMessage(
        eventId: Id,
        sequenceNum: Int,
        requestingUserId: Id,
    ): Result<Unit> =
        runCatching {
            if (sequenceNum < 0) throw ValidationException("Invalid sequence number.")

            val event = findEventOrFail(eventId)
            val message =
                messageRepository.getSingleByEventIdAndSeqNum(eventId, sequenceNum)
                    ?: throw NotFoundException(
                        "Message $sequenceNum in event '$eventId' not found.",
                    )

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

    private suspend fun findEventOrFail(eventId: Id): Event =
        eventRepository.getById(eventId)
            ?: throw NotFoundException("Event '$eventId' not found.")
}
