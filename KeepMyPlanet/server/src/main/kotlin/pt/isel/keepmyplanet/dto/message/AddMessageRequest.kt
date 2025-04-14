package pt.isel.keepmyplanet.dto.message

import kotlinx.serialization.Serializable

@Serializable
data class AddMessageRequest(
    val content: String,
)
