package pt.isel.keepmyplanet.dto.event

import kotlinx.serialization.Serializable
import pt.isel.keepmyplanet.domain.event.EventStatus
import pt.isel.keepmyplanet.dto.user.UserInfoSummaryResponse

@Serializable
data class EventStateChangeResponse(
    val id: UInt,
    val eventId: UInt,
    val newStatus: EventStatus,
    val changedBy: UserInfoSummaryResponse,
    val changeTime: String,
)
