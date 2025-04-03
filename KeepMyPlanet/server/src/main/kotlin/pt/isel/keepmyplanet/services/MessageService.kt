package pt.isel.keepmyplanet.services

import kotlinx.datetime.Clock
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import pt.isel.keepmyplanet.domain.common.Id
import pt.isel.keepmyplanet.domain.message.Message
import pt.isel.keepmyplanet.domain.message.MessageContent
import pt.isel.keepmyplanet.repository.MessageRepository

class MessageService(
    private val messageRepository: MessageRepository,
) {
    suspend fun getMessages(eventId: Id): Either<ChatError, List<Message>> =
        if (eventId.value == 0u) {
            Either.Left(ChatError.EventNotFound)
        } else {
            Either.Right(messageRepository.findByEventId(eventId))
        }

    suspend fun addMessage(
        eventId: Id,
        senderId: String,
        content: MessageContent,
    ): Either<ChatError, Message> =
        when {
            eventId.value == 0u -> Either.Left(ChatError.EventNotFound)
            senderId.isBlank() -> Either.Left(ChatError.UserNotFound)
            content.value.isBlank() -> Either.Left(ChatError.InvalidMessageContent)
            else -> {
                val newMessage =
                    Message(
                        id = Id((messageRepository.findByEventId(eventId).size + 1).toUInt()),
                        eventId = eventId,
                        senderId = Id(senderId.toUInt()),
                        content = content,
                        timestamp = Clock.System.now().toLocalDateTime(TimeZone.UTC),
                    )
                messageRepository.create(newMessage)
                Either.Right(newMessage)
            }
        }
}
