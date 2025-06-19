package pt.isel.keepmyplanet.domain.event

import kotlinx.datetime.LocalDateTime
import pt.isel.keepmyplanet.domain.common.Id

data class EventStateChange(
    val id: Id,
    val eventId: Id,
    val newStatus: EventStatus,
    val changedBy: Id,
    val changeTime: LocalDateTime,
)
