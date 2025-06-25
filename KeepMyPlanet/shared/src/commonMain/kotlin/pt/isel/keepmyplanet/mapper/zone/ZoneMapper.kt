package pt.isel.keepmyplanet.mapper.zone

import kotlinx.datetime.LocalDateTime
import pt.isel.keepmyplanet.domain.common.Description
import pt.isel.keepmyplanet.domain.common.Id
import pt.isel.keepmyplanet.domain.zone.Location
import pt.isel.keepmyplanet.domain.zone.Zone
import pt.isel.keepmyplanet.domain.zone.ZoneSeverity
import pt.isel.keepmyplanet.domain.zone.ZoneStatus
import pt.isel.keepmyplanet.dto.zone.ZoneResponse
import pt.isel.keepmyplanet.utils.safeValueOf

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

fun ZoneResponse.toZone(): Zone =
    Zone(
        id = Id(id),
        location = Location(latitude, longitude),
        description = Description(description),
        reporterId = Id(reporterId),
        eventId = associatedEventId?.let { Id(it) },
        status = safeValueOf<ZoneStatus>(status) ?: ZoneStatus.REPORTED,
        zoneSeverity = safeValueOf<ZoneSeverity>(severity) ?: ZoneSeverity.UNKNOWN,
        photosIds = photosIds.map { Id(it) }.toSet(),
        createdAt = LocalDateTime.parse(createdAt),
        updatedAt = LocalDateTime.parse(updatedAt),
    )
