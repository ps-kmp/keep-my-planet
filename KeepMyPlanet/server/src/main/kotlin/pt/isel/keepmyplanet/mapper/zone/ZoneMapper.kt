package pt.isel.keepmyplanet.mapper.zone

import pt.isel.keepmyplanet.domain.zone.Zone
import pt.isel.keepmyplanet.dto.zone.ZoneResponse

fun Zone.toResponse(): ZoneResponse =
    ZoneResponse(
        id = id.value,
        latitude = location.latitude,
        longitude = location.longitude,
        description = description.value,
        reporterId = reporterId.value,
        associatedEventId = eventId?.value,
        status = status.name,
        severity = zoneSeverity.name,
        photosIds = photosIds.map { it.value }.toSet(),
        createdAt = createdAt.toString(),
        updatedAt = updatedAt.toString(),
    )
