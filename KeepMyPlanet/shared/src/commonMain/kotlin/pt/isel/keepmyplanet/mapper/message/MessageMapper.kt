package pt.isel.keepmyplanet.mapper.message

import kotlinx.datetime.LocalDateTime
import pt.isel.keepmyplanet.domain.common.Id
import pt.isel.keepmyplanet.domain.message.Message
import pt.isel.keepmyplanet.domain.message.MessageContent
import pt.isel.keepmyplanet.domain.user.Name
import pt.isel.keepmyplanet.dto.message.MessageResponse

fun Message.toResponse(): MessageResponse =
    MessageResponse(
        id = id.value,
        eventId = eventId.value,
        senderId = senderId.value,
        senderName = senderName.value,
        content = content.value,
        timestamp = timestamp.toString(),
        chatPosition = chatPosition,
    )

fun MessageResponse.toMessage(): Message =
    Message(
        id = Id(this.id),
        eventId = Id(this.eventId),
        content = MessageContent(this.content),
        senderId = Id(this.senderId),
        senderName = Name(this.senderName),
        timestamp = LocalDateTime.parse(this.timestamp),
        chatPosition = this.chatPosition,
    )
