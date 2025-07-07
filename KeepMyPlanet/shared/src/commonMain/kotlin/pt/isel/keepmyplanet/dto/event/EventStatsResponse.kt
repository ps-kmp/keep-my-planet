package pt.isel.keepmyplanet.dto.event

import kotlinx.serialization.Serializable

@Serializable
data class EventStatsResponse(
    val totalParticipants: Int,
    val totalAttendees: Int,
    val checkInRate: Double,
    val totalHoursVolunteered: Double,
)
