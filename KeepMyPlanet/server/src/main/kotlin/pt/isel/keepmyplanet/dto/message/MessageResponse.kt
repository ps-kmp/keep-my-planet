package pt.isel.keepmyplanet.dto.message

import kotlinx.serialization.Serializable

@Serializable
data class MessageResponse(
    val chatPosition: Int,
    val eventId: UInt,
    val senderId: UInt,
    val content: String,
    val timestamp: String,
)
