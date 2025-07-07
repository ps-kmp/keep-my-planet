package pt.isel.keepmyplanet.domain.event

data class EventStats(
    val totalParticipants: Int,
    val totalAttendees: Int,
    val checkInRate: Double,
    val totalHoursVolunteered: Double,
)
