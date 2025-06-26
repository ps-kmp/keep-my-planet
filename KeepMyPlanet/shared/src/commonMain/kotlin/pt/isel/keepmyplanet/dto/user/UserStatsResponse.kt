package pt.isel.keepmyplanet.dto.user

import kotlinx.serialization.Serializable

@Serializable
data class UserStatsResponse(
    val totalEventsAttended: Int,
    val totalHoursVolunteered: Double,
)
