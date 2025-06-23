package pt.isel.keepmyplanet.domain.event

import kotlinx.datetime.LocalDateTime
import pt.isel.keepmyplanet.domain.common.Description
import pt.isel.keepmyplanet.domain.common.Id
import pt.isel.keepmyplanet.util.isAfter
import pt.isel.keepmyplanet.util.isBefore
import pt.isel.keepmyplanet.util.now

data class Event(
    val id: Id,
    val title: Title,
    val description: Description,
    val period: Period,
    val zoneId: Id,
    val organizerId: Id,
    private val persistedStatus: EventStatus = EventStatus.PLANNED,
    val maxParticipants: Int? = null,
    val participantsIds: Set<Id> = emptySet(),
    val createdAt: LocalDateTime,
    val updatedAt: LocalDateTime,
) {
    val status: EventStatus
        get() {
            if (persistedStatus in listOf(EventStatus.COMPLETED, EventStatus.CANCELLED)) {
                return persistedStatus
            }
            val now = now()
            if (now.isAfter(period.start)) {
                if (period.end == null || now.isBefore(period.end)) {
                    return EventStatus.IN_PROGRESS
                }
            }
            return persistedStatus
        }

    fun getPersistedStatus(): EventStatus = persistedStatus

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

    fun isUserParticipantOrOrganizer(userId: Id): Boolean =
        userId in participantsIds || userId == organizerId
}
