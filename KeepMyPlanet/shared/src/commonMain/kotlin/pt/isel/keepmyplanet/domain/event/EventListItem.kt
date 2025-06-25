package pt.isel.keepmyplanet.domain.event

import pt.isel.keepmyplanet.domain.common.Description
import pt.isel.keepmyplanet.domain.common.Id

data class EventListItem(
    val id: Id,
    val title: Title,
    val description: Description,
    val period: Period,
    val status: EventStatus,
    val participantCount: Int,
    val maxParticipants: Int?,
)
