package pt.isel.keepmyplanet.mapper.event

import pt.isel.keepmyplanet.domain.event.Event
import pt.isel.keepmyplanet.dto.event.CreateEventResponse

fun Event.toResponse() =
    CreateEventResponse(
        id = id.value,
        title = title.value,
        description = description.value,
        periodStart = period.start.toString(),
        periodEnd = period.end.toString(),
        zoneId = zoneId.value,
        organizerId = organizerId.value,
        status = status,
        maxParticipants = maxParticipants,
        participantsIds = participantsIds.map { it.value }.toSet(),
        createdAt = createdAt.toString(),
        updatedAt = updatedAt?.toString(),
    )
