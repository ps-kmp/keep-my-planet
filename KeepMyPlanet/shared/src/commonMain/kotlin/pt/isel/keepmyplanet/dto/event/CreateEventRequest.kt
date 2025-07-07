package pt.isel.keepmyplanet.dto.event

import kotlinx.serialization.Serializable

@Serializable
data class CreateEventRequest(
    val title: String,
    val description: String,
    val startDate: String,
    val endDate: String? = null,
    val zoneId: UInt,
    val maxParticipants: Int? = null,
)

@Serializable
data class ManualNotificationRequest(
    val title: String,
    val message: String,
)
