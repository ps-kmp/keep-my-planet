package pt.isel.keepmyplanet.domain.zone

import kotlinx.datetime.LocalDateTime
import pt.isel.keepmyplanet.domain.common.Description
import pt.isel.keepmyplanet.domain.common.Id
import pt.isel.keepmyplanet.domain.common.Location

data class Zone(
    val id: Id,
    val location: Location,
    // val address: Address,
    val description: Description,
    val reporterId: Id,
    val status: ZoneStatus = ZoneStatus.REPORTED,
    val severity: Severity? = null,
    val photosIds: Set<Id> = emptySet(),
    val createdAt: LocalDateTime,
    val updatedAt: LocalDateTime,
)
