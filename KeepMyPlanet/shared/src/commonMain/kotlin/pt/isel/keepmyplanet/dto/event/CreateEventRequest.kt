package pt.isel.keepmyplanet.dto.event

import kotlinx.serialization.Serializable

@Serializable
data class CreateEventRequest(
    val title: String,
    val description: String,
    val periodStart: String,
    val periodEnd: String,
    val maxParticipants: Int? = null,
)
