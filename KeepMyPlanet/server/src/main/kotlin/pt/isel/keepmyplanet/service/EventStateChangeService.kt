package pt.isel.keepmyplanet.service

import pt.isel.keepmyplanet.domain.common.Id
import pt.isel.keepmyplanet.domain.event.Event
import pt.isel.keepmyplanet.domain.event.EventStateChange
import pt.isel.keepmyplanet.domain.event.EventStatus
import pt.isel.keepmyplanet.domain.zone.ZoneStatus
import pt.isel.keepmyplanet.dto.event.EventStateChangeResponse
import pt.isel.keepmyplanet.dto.user.UserInfoSummaryResponse
import pt.isel.keepmyplanet.exception.AuthorizationException
import pt.isel.keepmyplanet.exception.ConflictException
import pt.isel.keepmyplanet.exception.NotFoundException
import pt.isel.keepmyplanet.repository.EventRepository
import pt.isel.keepmyplanet.repository.EventStateChangeRepository
import pt.isel.keepmyplanet.repository.ZoneRepository
import pt.isel.keepmyplanet.utils.now

class EventStateChangeService(
    private val eventRepository: EventRepository,
    private val zoneRepository: ZoneRepository,
    private val eventStateChangeRepository: EventStateChangeRepository,
    private val notificationService: NotificationService,
    private val zoneStateChangeService: ZoneStateChangeService,
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
                if (newStatus == EventStatus.COMPLETED && event.period.end == null) {
                    event.copy(status = newStatus, period = event.period.copy(end = now))
                } else {
                    event.copy(status = newStatus)
                }
            eventRepository.update(updatedEvent)
            handleZoneStatusChange(event, newStatus, requestingUserId)

            val stateChange =
                EventStateChange(
                    id = Id(0u),
                    eventId = eventId,
                    newStatus = newStatus,
                    changedBy = requestingUserId,
                    changeTime = now,
                )
            eventStateChangeRepository.save(stateChange)

            if (newStatus == EventStatus.CANCELLED) {
                val notificationData =
                    mapOf(
                        "title" to "Event Cancelled: ${event.title.value}",
                        "body" to
                            "The event '${event.title.value}' has been cancelled by the organizer.",
                        "eventId" to event.id.value.toString(),
                        "type" to "EVENT_CANCELLED",
                    )
                val topic = "event_${event.id.value}"
                notificationService.sendNotificationToTopic(topic, notificationData)
            }

            updatedEvent
        }

    private suspend fun handleZoneStatusChange(
        event: Event,
        newStatus: EventStatus,
        requestingUserId: Id,
    ) {
        val zone =
            zoneRepository.getById(event.zoneId)
                ?: throw NotFoundException("Zone '${event.zoneId}' not found.")

        if (zone.eventId != event.id) return

        when (newStatus) {
            EventStatus.CANCELLED -> {
                val updatedZone =
                    zoneStateChangeService.changeZoneStatus(
                        zone = zone,
                        newStatus = ZoneStatus.REPORTED,
                        changedBy = requestingUserId,
                        triggeredByEventId = event.id,
                    )
                zoneRepository.update(updatedZone.copy(eventId = null))
            }

            EventStatus.COMPLETED -> {
                val cleanedZone =
                    zoneStateChangeService.changeZoneStatus(
                        zone = zone,
                        newStatus = ZoneStatus.CLEANED,
                        changedBy = requestingUserId,
                        triggeredByEventId = event.id,
                    )
                val finalZone = cleanedZone.copy(eventId = null)
                zoneRepository.update(finalZone)
            }

            else -> {
                // No action needed for other statuses
            }
        }
    }

    suspend fun getEventStateChanges(eventId: Id): Result<List<EventStateChangeResponse>> =
        runCatching {
            findEventOrFail(eventId)
            val changesWithDetails = eventStateChangeRepository.findByEventIdWithDetails(eventId)

            changesWithDetails.map { details ->
                EventStateChangeResponse(
                    id = details.stateChange.id.value,
                    eventId = details.stateChange.eventId.value,
                    newStatus = details.stateChange.newStatus,
                    changedBy =
                        UserInfoSummaryResponse(
                            id = details.stateChange.changedBy.value,
                            name = details.changedByName.value,
                        ),
                    changeTime = details.stateChange.changeTime.toString(),
                )
            }
        }

    private fun isValidTransition(
        from: EventStatus,
        to: EventStatus,
    ): Boolean =
        when (from) {
            EventStatus.PLANNED -> to in listOf(EventStatus.IN_PROGRESS, EventStatus.CANCELLED)
            EventStatus.IN_PROGRESS -> to in listOf(EventStatus.COMPLETED, EventStatus.CANCELLED)
            EventStatus.COMPLETED, EventStatus.CANCELLED, EventStatus.UNKNOWN -> false
        }

    private fun canUserChangeStatus(
        userId: Id,
        event: Event,
    ): Boolean = userId == event.organizerId

    private suspend fun findEventOrFail(eventId: Id): Event =
        eventRepository.getById(eventId)
            ?: throw NotFoundException("Event '$eventId' not found.")

    suspend fun transitionEventsToInProgress() {
        val eventsToStart = eventRepository.findEventsToStart()
        eventsToStart.forEach { event ->
            changeEventStatus(event.id, EventStatus.IN_PROGRESS, event.organizerId)
                .onSuccess {
                    runCatching {
                        if (!eventRepository.hasAttended(event.id, event.organizerId)) {
                            eventRepository.addAttendance(event.id, event.organizerId, now())
                        }
                    }.onFailure { e ->
                        println(
                            "Failed to auto-check-in organizer ${event.organizerId} " +
                                "for event ${event.id}: $e",
                        )
                    }
                }.onFailure {
                    println("Failed to transition event ${event.id} to IN_PROGRESS: $it")
                }
        }
    }
}
