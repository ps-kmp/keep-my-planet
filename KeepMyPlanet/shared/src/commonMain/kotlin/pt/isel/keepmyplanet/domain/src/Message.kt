package pt.isel.keepmyplanet.domain.src

import kotlinx.datetime.LocalDateTime

data class Message(
    val id: Id,
    val content: MessageContent,
    val timestamp: LocalDateTime,
    val sender: User,
    val event: Event,
)
