package pt.isel.keepmyplanet.domain.message

import kotlinx.datetime.LocalDateTime
import pt.isel.keepmyplanet.domain.common.Id

data class Message(
    val chatPosition: Int,
    val eventId: Id,
    val senderId: Id,
    val content: MessageContent,
    val timestamp: LocalDateTime,
)
