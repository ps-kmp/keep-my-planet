package pt.isel.keepmyplanet.domain.message

import kotlinx.datetime.LocalDateTime
import pt.isel.keepmyplanet.domain.common.Id
import pt.isel.keepmyplanet.domain.user.Name

data class Message(
    val id: Id,
    val eventId: Id,
    val senderId: Id,
    val senderName: Name,
    val content: MessageContent,
    val timestamp: LocalDateTime,
    val chatPosition: Int,
)
