package pt.isel.keepmyplanet.dto.event

import kotlinx.serialization.Serializable

@Serializable
data class EventResponse(
    val id: UInt,
    val title: String,
    val description: String,
    val startDate: String,
    val endDate: String,
    val zoneId: UInt,
    val organizerId: UInt,
    val status: String,
    val maxParticipants: Int? = null,
    val participantsIds: Set<UInt>,
    val createdAt: String,
    val updatedAt: String,
)
