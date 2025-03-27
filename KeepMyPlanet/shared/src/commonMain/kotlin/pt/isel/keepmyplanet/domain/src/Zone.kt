package pt.isel.keepmyplanet.domain.src

import kotlinx.datetime.LocalDateTime

data class Zone(
    val id: Id,
    val address: Address,
    val description: Description,
    val area: Location,
    val reported: LocalDateTime,
    val photos: List<Photo>,
    val critical: Boolean,
    val reporter: User,
    val status: ZoneStatus,
    val events: List<Event>,
)
