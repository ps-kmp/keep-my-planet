package pt.isel.keepmyplanet.mapper.message

import pt.isel.keepmyplanet.domain.message.Message
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
