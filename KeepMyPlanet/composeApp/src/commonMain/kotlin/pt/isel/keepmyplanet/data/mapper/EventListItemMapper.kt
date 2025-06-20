package pt.isel.keepmyplanet.data.mapper

import kotlinx.datetime.LocalDateTime
import pt.isel.keepmyplanet.domain.common.Description
import pt.isel.keepmyplanet.domain.common.Id
import pt.isel.keepmyplanet.domain.event.Event
import pt.isel.keepmyplanet.domain.event.EventStatus
import pt.isel.keepmyplanet.domain.event.Period
import pt.isel.keepmyplanet.domain.event.Title
import pt.isel.keepmyplanet.dto.event.EventResponse
import pt.isel.keepmyplanet.ui.event.list.model.EventListItem
import pt.isel.keepmyplanet.util.safeValueOf

fun EventResponse.toListItem(): EventListItem =
    EventListItem(
        id = Id(id),
        title = Title(title),
        description = Description(description),
        period = Period(LocalDateTime.parse(startDate), endDate?.let { LocalDateTime.parse(it) }),
        status = safeValueOf<EventStatus>(status) ?: EventStatus.UNKNOWN,
        participantCount = participantsIds.size,
        maxParticipants = maxParticipants,
    )

fun Event.toListItem(): EventListItem =
    EventListItem(
        id = this.id,
        title = this.title,
        description = this.description,
        period = this.period,
        status = this.status,
        participantCount = this.participantsIds.size,
        maxParticipants = this.maxParticipants
    )
