package pt.isel.keepmyplanet.domain.event

import kotlinx.datetime.LocalDateTime
import pt.isel.keepmyplanet.domain.common.Description
import pt.isel.keepmyplanet.domain.common.Id

data class Event(
    val id: Id,
    val title: Title,
    val description: Description,
    val period: Period,
    val zoneId: Id,
    val organizerId: Id,
    val status: EventStatus = EventStatus.PLANNED,
    val maxParticipants: Int? = null, // null means no limit
    val participantsIds: Set<Id> = emptySet(),
    val createdAt: LocalDateTime,
    val updatedAt: LocalDateTime,
) {
    init {
        maxParticipants?.let {
            require(it > 0) { "Max participants must be positive if set" }
        }
        require(participantsIds.size <= (maxParticipants ?: Int.MAX_VALUE)) {
            "Number of participants cannot exceed maxParticipants"
        }
    }

    val isFull: Boolean
        get() = maxParticipants?.let { participantsIds.size >= it } ?: false
}
