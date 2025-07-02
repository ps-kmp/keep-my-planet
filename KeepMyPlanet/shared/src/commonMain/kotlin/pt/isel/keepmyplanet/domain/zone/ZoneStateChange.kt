package pt.isel.keepmyplanet.domain.zone

import kotlinx.datetime.LocalDateTime
import pt.isel.keepmyplanet.domain.common.Id

data class ZoneStateChange(
    val id: Id,
    val zoneId: Id,
    val newStatus: ZoneStatus,
    val changedBy: Id?,
    val triggeredByEventId: Id?,
    val changeTime: LocalDateTime,
)
