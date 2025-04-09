package pt.isel.keepmyplanet.mapper.zone

import pt.isel.keepmyplanet.domain.zone.Zone
import pt.isel.keepmyplanet.dto.zone.ZoneResponse

fun Zone.toResponse(): ZoneResponse =
    ZoneResponse(
        id = this.id.value,
        latitude = this.location.latitude,
        longitude = this.location.longitude,
        description = this.description.value,
        reporterId = this.reporterId.value,
        associatedEventId = this.eventId?.value,
        status = this.status.name,
        severity = this.severity.name,
        photosIds = this.photosIds.map { it.value }.toSet(),
        createdAt = this.createdAt.toString(),
        updatedAt = this.updatedAt.toString(),
    )
