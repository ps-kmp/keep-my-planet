package pt.isel.keepmyplanet.mapper.message

import pt.isel.keepmyplanet.domain.message.Message
import pt.isel.keepmyplanet.dtos.message.MessageResponse

fun Message.toResponse(): MessageResponse =
    MessageResponse(
        id = id.value,
        eventId = eventId.value,
        senderId = senderId.value,
        content = content.value,
        timestamp = timestamp.toString(),
        chatPosition = chatPosition,
    )
