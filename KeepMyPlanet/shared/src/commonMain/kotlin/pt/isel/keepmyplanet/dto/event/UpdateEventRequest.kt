package pt.isel.keepmyplanet.dto.event

import kotlinx.serialization.Serializable

@Serializable
data class UpdateEventRequest(
    val title: String? = null,
    val description: String? = null,
    val startDate: String? = null,
    val endDate: String? = null,
    val maxParticipants: Int? = null,
)
