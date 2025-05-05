package pt.isel.keepmyplanet.dto.message

import kotlinx.serialization.Serializable

@Serializable
data class MessageResponse(
    val id: UInt,
    val eventId: UInt,
    val senderId: UInt,
    val senderName: String,
    val content: String,
    val timestamp: String,
    val chatPosition: Int,
)
