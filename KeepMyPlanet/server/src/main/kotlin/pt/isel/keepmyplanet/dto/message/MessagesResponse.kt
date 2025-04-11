package pt.isel.keepmyplanet.dto.message

import kotlinx.serialization.Serializable

@Serializable
data class MessagesResponse(
    val messages: List<MessageResponse>,
)
