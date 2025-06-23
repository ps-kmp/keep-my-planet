package pt.isel.keepmyplanet.dto.event

import kotlinx.serialization.Serializable
import pt.isel.keepmyplanet.domain.event.EventStatus

@Serializable
data class EventStateChangeResponse(
    val id: UInt,
    val eventId: UInt,
    val newStatus: EventStatus,
    val changedBy: UserInfoSummaryResponse,
    val changeTime: String,
)

@Serializable
data class UserInfoSummaryResponse(
    val id: UInt,
    val name: String,
)
