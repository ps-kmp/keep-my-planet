package pt.isel.keepmyplanet.mapper.user

import pt.isel.keepmyplanet.domain.user.UserStats
import pt.isel.keepmyplanet.dto.user.UserStatsResponse

fun UserStats.toResponse() =
    UserStatsResponse(
        totalEventsAttended = this.totalEventsAttended,
        totalHoursVolunteered = this.totalHoursVolunteered,
    )

fun UserStatsResponse.toDomain() =
    UserStats(
        totalEventsAttended = this.totalEventsAttended,
        totalHoursVolunteered = this.totalHoursVolunteered,
    )
