package pt.isel.keepmyplanet.mapper.zone

import kotlinx.datetime.LocalDateTime
import pt.isel.keepmyplanet.domain.common.Description
import pt.isel.keepmyplanet.domain.common.Id
import pt.isel.keepmyplanet.domain.zone.Location
import pt.isel.keepmyplanet.domain.zone.Radius
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
        radius = radius.value,
        description = description.value,
        reporterId = reporterId.value,
        eventId = eventId?.value,
        status = status.name,
        severity = zoneSeverity.name,
        beforePhotosIds = beforePhotosIds.map { it.value }.toSet(),
        afterPhotosIds = afterPhotosIds.map { it.value }.toSet(),
        createdAt = createdAt.toString(),
        updatedAt = updatedAt.toString(),
    )

fun ZoneResponse.toZone(): Zone =
    Zone(
        id = Id(id),
        location = Location(latitude, longitude),
        radius = Radius(radius),
        description = Description(description),
        reporterId = Id(reporterId),
        eventId = eventId?.let { Id(it) },
        status = safeValueOf<ZoneStatus>(status) ?: ZoneStatus.REPORTED,
        zoneSeverity = safeValueOf<ZoneSeverity>(severity) ?: ZoneSeverity.UNKNOWN,
        isActive = true,
        beforePhotosIds = beforePhotosIds.map { Id(it) }.toSet(),
        afterPhotosIds = afterPhotosIds.map { Id(it) }.toSet(),
        createdAt = LocalDateTime.parse(createdAt),
        updatedAt = LocalDateTime.parse(updatedAt),
    )
