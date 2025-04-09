package pt.isel.keepmyplanet.domain.zone

import kotlinx.datetime.LocalDateTime
import pt.isel.keepmyplanet.domain.common.Description
import pt.isel.keepmyplanet.domain.common.Id
import pt.isel.keepmyplanet.domain.common.Location

data class Zone(
    val id: Id,
    val location: Location,
    val description: Description,
    val reporterId: Id,
    val eventId: Id? = null,
    val status: ZoneStatus = ZoneStatus.REPORTED,
    val severity: Severity = Severity.UNKNOWN,
    val photosIds: Set<Id>,
    val createdAt: LocalDateTime,
    val updatedAt: LocalDateTime,
)
