@file:Suppress("ktlint:standard:filename")

package pt.isel.keepmyplanet.mapper.event

import pt.isel.keepmyplanet.domain.event.EventStats
import pt.isel.keepmyplanet.dto.event.EventStatsResponse

fun EventStatsResponse.toDomain(): EventStats =
    EventStats(
        totalParticipants = totalParticipants,
        totalAttendees = totalAttendees,
        checkInRate = checkInRate,
        totalHoursVolunteered = totalHoursVolunteered,
    )
