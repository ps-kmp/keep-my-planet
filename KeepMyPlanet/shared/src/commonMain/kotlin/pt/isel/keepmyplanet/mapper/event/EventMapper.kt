package pt.isel.keepmyplanet.mapper.event

import kotlinx.datetime.LocalDateTime
import pt.isel.keepmyplanet.domain.common.Description
import pt.isel.keepmyplanet.domain.common.Id
import pt.isel.keepmyplanet.domain.event.Event
import pt.isel.keepmyplanet.domain.event.EventStatus
import pt.isel.keepmyplanet.domain.event.Period
import pt.isel.keepmyplanet.domain.event.Title
import pt.isel.keepmyplanet.dto.event.EventResponse
import pt.isel.keepmyplanet.util.safeValueOf

fun Event.toResponse() =
    EventResponse(
        id = id.value,
        title = title.value,
        description = description.value,
        startDate = period.start.toString(),
        endDate = period.end?.toString(),
        zoneId = zoneId.value,
        organizerId = organizerId.value,
        status = status.name,
        maxParticipants = maxParticipants,
        participantsIds = participantsIds.map { it.value }.toSet(),
        createdAt = createdAt.toString(),
        updatedAt = updatedAt.toString(),
    )

fun EventResponse.toEvent(): Event =
    Event(
        id = Id(id),
        title = Title(title),
        description = Description(description),
        period =
            Period(
                start = LocalDateTime.parse(startDate),
                end = endDate?.let { LocalDateTime.parse(it) },
            ),
        persistedStatus = safeValueOf<EventStatus>(status) ?: EventStatus.UNKNOWN,
        maxParticipants = maxParticipants,
        organizerId = Id(organizerId),
        participantsIds = participantsIds.map { Id(it) }.toSet(),
        createdAt = LocalDateTime.parse(createdAt),
        updatedAt = LocalDateTime.parse(updatedAt),
        zoneId = Id(zoneId),
    )
