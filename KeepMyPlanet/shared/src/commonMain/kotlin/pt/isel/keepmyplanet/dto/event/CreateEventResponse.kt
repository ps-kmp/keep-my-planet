package pt.isel.keepmyplanet.dto.event

import kotlinx.serialization.Serializable
import pt.isel.keepmyplanet.domain.event.EventStatus

@Serializable
data class CreateEventResponse(
    val id: UInt,
    val title: String,
    val description: String,
    val periodStart: String,
    val periodEnd: String,
    val zoneId: UInt,
    val organizerId: UInt,
    val status: EventStatus,
    val maxParticipants: Int? = null,
    val participantsIds: Set<UInt>,
    val createdAt: String,
    val updatedAt: String? = null,
)
