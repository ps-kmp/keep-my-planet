package pt.isel.keepmyplanet.dtos.message

import kotlinx.serialization.Serializable

@Serializable
data class AddMessageRequest(
    val content: String,
)
