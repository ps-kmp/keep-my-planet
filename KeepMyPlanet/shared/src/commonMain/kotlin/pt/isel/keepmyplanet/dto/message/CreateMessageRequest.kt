package pt.isel.keepmyplanet.dto.message

import kotlinx.serialization.Serializable

@Serializable
data class CreateMessageRequest(
    val content: String,
)
