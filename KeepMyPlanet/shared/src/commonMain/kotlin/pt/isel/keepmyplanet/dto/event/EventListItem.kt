package pt.isel.keepmyplanet.dto.event

import pt.isel.keepmyplanet.domain.common.Description
import pt.isel.keepmyplanet.domain.common.Id
import pt.isel.keepmyplanet.domain.event.EventStatus
import pt.isel.keepmyplanet.domain.event.Period
import pt.isel.keepmyplanet.domain.event.Title

data class EventListItem(
    val id: Id,
    val title: Title,
    val description: Description,
    val period: Period,
    val status: EventStatus,
    val participantCount: Int,
    val maxParticipants: Int?,
)
