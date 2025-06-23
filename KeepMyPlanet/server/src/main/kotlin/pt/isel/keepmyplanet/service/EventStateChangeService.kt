package pt.isel.keepmyplanet.service

import pt.isel.keepmyplanet.domain.common.Id
import pt.isel.keepmyplanet.domain.event.Event
import pt.isel.keepmyplanet.domain.event.EventStateChange
import pt.isel.keepmyplanet.domain.event.EventStatus
import pt.isel.keepmyplanet.domain.zone.ZoneStatus
import pt.isel.keepmyplanet.errors.AuthorizationException
import pt.isel.keepmyplanet.errors.ConflictException
import pt.isel.keepmyplanet.errors.NotFoundException
import pt.isel.keepmyplanet.repository.EventRepository
import pt.isel.keepmyplanet.repository.EventStateChangeRepository
import pt.isel.keepmyplanet.repository.ZoneRepository
import pt.isel.keepmyplanet.util.now

class EventStateChangeService(
    private val eventRepository: EventRepository,
    private val zoneRepository: ZoneRepository,
    private val eventStateChangeRepository: EventStateChangeRepository,
) {
    suspend fun changeEventStatus(
        eventId: Id,
        newStatus: EventStatus,
        requestingUserId: Id,
    ): Result<Event> =
        runCatching {
            val event = findEventOrFail(eventId)

            if (!canUserChangeStatus(requestingUserId, event)) {
                throw AuthorizationException(
                    "User $requestingUserId is not authorized to change event status.",
                )
            }

            if (!isValidTransition(event.status, newStatus)) {
                throw ConflictException("Invalid status transition: ${event.status} → $newStatus.")
            }

            val now = now()

            val updatedEvent =
                event.copy(
                    persistedStatus = newStatus,
                )
            eventRepository.update(updatedEvent)

            handleZoneStatusChange(event, newStatus)

            val stateChange =
                EventStateChange(
                    id = Id(0u),
                    eventId = eventId,
                    newStatus = newStatus,
                    changedBy = requestingUserId,
                    changeTime = now,
                )
            eventStateChangeRepository.save(stateChange)

            updatedEvent
        }

    private suspend fun handleZoneStatusChange(event: Event, newStatus: EventStatus) {
        val zone = zoneRepository.getById(event.zoneId)
            ?: throw NotFoundException("Zone '${event.zoneId}' associated with event not found.")

        if (zone.eventId != event.id) return

        val updatedZone = when (newStatus) {
            EventStatus.CANCELLED -> zone.copy(eventId = null, status = ZoneStatus.REPORTED)
            EventStatus.COMPLETED -> zone.copy(eventId = null, status = ZoneStatus.CLEANED)
            else -> null
        }

        updatedZone?.let {
            zoneRepository.update(it)
        }
    }

    suspend fun getEventStateChanges(eventId: Id): Result<List<EventStateChange>> =
        runCatching {
            findEventOrFail(eventId)
            eventStateChangeRepository.findByEventId(eventId)
        }

    private fun isValidTransition(
        from: EventStatus,
        to: EventStatus,
    ): Boolean =
        when (from) {
            EventStatus.PLANNED -> to == EventStatus.CANCELLED
            EventStatus.IN_PROGRESS -> to in listOf(EventStatus.COMPLETED, EventStatus.CANCELLED)
            EventStatus.COMPLETED, EventStatus.CANCELLED, EventStatus.UNKNOWN -> false
        }

    private fun canUserChangeStatus(
        userId: Id,
        event: Event,
    ): Boolean {
        return userId == event.organizerId // Only organizer can change status
    }

    private suspend fun findEventOrFail(eventId: Id): Event =
        eventRepository.getById(eventId)
            ?: throw NotFoundException("Event '$eventId' not found.")
}
