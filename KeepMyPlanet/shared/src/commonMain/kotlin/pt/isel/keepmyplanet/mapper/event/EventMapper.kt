package pt.isel.keepmyplanet.mapper.event

import pt.isel.keepmyplanet.domain.event.Event
import pt.isel.keepmyplanet.dto.event.EventResponse

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
