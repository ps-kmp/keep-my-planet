package pt.isel.keepmyplanet.mapper.message

import pt.isel.keepmyplanet.domain.message.Message
import pt.isel.keepmyplanet.dto.message.MessageResponse

fun Message.toResponse(): MessageResponse =
    MessageResponse(
        chatPosition = chatPosition,
        eventId = eventId.value,
        senderId = senderId.value,
        content = content.value,
        timestamp = timestamp.toString(),
    )
