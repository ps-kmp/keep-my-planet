package pt.isel.keepmyplanet.dto.event

import kotlinx.serialization.Serializable
import pt.isel.keepmyplanet.domain.event.EventStatus

@Serializable
data class EventStateChangeResponse(
    val id: UInt,
    val eventId: UInt,
    val newStatus: EventStatus,
    val changedBy: UInt,
    val changeTime: String,
)
