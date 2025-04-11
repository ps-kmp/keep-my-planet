package pt.isel.keepmyplanet.dto.message

import kotlinx.serialization.Serializable

@Serializable
data class AddMessageRequest(
    val senderId: UInt,
    val content: String,
)
