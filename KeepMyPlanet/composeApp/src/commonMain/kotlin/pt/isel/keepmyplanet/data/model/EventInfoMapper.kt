package pt.isel.keepmyplanet.data.model

import kotlinx.datetime.LocalDateTime
import pt.isel.keepmyplanet.domain.common.Description
import pt.isel.keepmyplanet.domain.common.Id
import pt.isel.keepmyplanet.domain.event.EventStatus
import pt.isel.keepmyplanet.domain.event.Period
import pt.isel.keepmyplanet.domain.event.Title
import pt.isel.keepmyplanet.dto.event.EventResponse

fun EventResponse.toEventInfo() =
    EventInfo(
        id = Id(id),
        title = Title(title),
        description = Description(description),
        period =
            Period(
                LocalDateTime.parse(startDate),
                endDate?.let { LocalDateTime.parse(it) },
            ),
        status = EventStatus.valueOf(status.uppercase()),
    )
