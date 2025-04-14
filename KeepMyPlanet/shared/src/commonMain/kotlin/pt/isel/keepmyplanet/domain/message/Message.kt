package pt.isel.keepmyplanet.domain.message

import kotlinx.datetime.LocalDateTime
import pt.isel.keepmyplanet.domain.common.Id

data class Message(
    val id: Id,
    val eventId: Id,
    val senderId: Id,
    val content: MessageContent,
    val timestamp: LocalDateTime,
    val chatPosition: Int,
)
