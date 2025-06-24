package pt.isel.keepmyplanet.service

import pt.isel.keepmyplanet.domain.common.Description
import pt.isel.keepmyplanet.domain.common.Id
import pt.isel.keepmyplanet.domain.event.Event
import pt.isel.keepmyplanet.domain.event.EventStatus
import pt.isel.keepmyplanet.domain.event.Period
import pt.isel.keepmyplanet.domain.event.Title
import pt.isel.keepmyplanet.domain.user.User
import pt.isel.keepmyplanet.domain.zone.Zone
import pt.isel.keepmyplanet.domain.zone.ZoneStatus
import pt.isel.keepmyplanet.errors.AuthorizationException
import pt.isel.keepmyplanet.errors.ConflictException
import pt.isel.keepmyplanet.errors.InternalServerException
import pt.isel.keepmyplanet.errors.NotFoundException
import pt.isel.keepmyplanet.errors.ValidationException
import pt.isel.keepmyplanet.repository.EventRepository
import pt.isel.keepmyplanet.repository.MessageRepository
import pt.isel.keepmyplanet.repository.UserRepository
import pt.isel.keepmyplanet.repository.ZoneRepository
import pt.isel.keepmyplanet.util.now

class EventService(
    private val eventRepository: EventRepository,
    private val zoneRepository: ZoneRepository,
    private val userRepository: UserRepository,
    private val messageRepository: MessageRepository,
) {
    suspend fun createEvent(
        title: Title,
        description: Description,
        period: Period,
        zoneId: Id,
        organizerId: Id,
        maxParticipants: Int?,
    ): Result<Event> =
        runCatching {
            findUserOrFail(organizerId)
            val zone = findZoneOrFail(zoneId)

            if (zone.eventId != null) {
                throw ConflictException("Zone '$zoneId' is already associated with an event.")
            }

            if (period.start < now()) {
                throw ValidationException("Event start date must be in the future.")
            }

            if (maxParticipants != null && maxParticipants < 1) {
                throw ValidationException("Max participants must be at least 1.")
            }

            val currentTime = now()
            val event =
                Event(
                    id = Id(0u),
                    title = title,
                    description = description,
                    period = period,
                    zoneId = zoneId,
                    organizerId = organizerId,
                    participantsIds = setOf(organizerId),
                    maxParticipants = maxParticipants,
                    createdAt = currentTime,
                    updatedAt = currentTime,
                )
            val createdEvent = eventRepository.create(event)

            val updatedZone =
                zone.copy(eventId = createdEvent.id, status = ZoneStatus.CLEANING_SCHEDULED)
            zoneRepository.update(updatedZone)

            createdEvent
        }

    suspend fun getEventDetails(eventId: Id): Result<Event> =
        runCatching {
            findEventOrFail(eventId)
        }

    suspend fun searchAllEvents(
        name: String?,
        limit: Int,
        offset: Int,
    ): Result<List<Event>> =
        runCatching {
            if (name.isNullOrBlank()) {
                eventRepository.getAll(limit, offset)
            } else {
                eventRepository.findByName(name, limit, offset)
            }
        }

    suspend fun getEventsOrganizedBy(
        userId: Id,
        query: String?,
        limit: Int,
        offset: Int,
    ): Result<List<Event>> =
        runCatching {
            findUserOrFail(userId)
            if (query.isNullOrBlank()) {
                eventRepository.findByOrganizerId(userId, limit, offset)
            } else {
                eventRepository.findByNameAndOrganizerId(userId, query, limit, offset)
            }
        }

    suspend fun getEventsJoinedBy(
        userId: Id,
        query: String?,
        limit: Int,
        offset: Int,
    ): Result<List<Event>> =
        runCatching {
            findUserOrFail(userId)
            if (query.isNullOrBlank()) {
                eventRepository.findByParticipantId(userId, limit, offset)
            } else {
                eventRepository.findByNameAndParticipantId(userId, query, limit, offset)
            }
        }

    suspend fun updateEventDetails(
        eventId: Id,
        userId: Id,
        title: Title?,
        description: Description?,
        period: Period?,
        maxParticipants: Int?,
    ): Result<Event> =
        runCatching {
            val event = findEventOrFail(eventId)
            ensureOrganizerOrFail(event, userId)

            if (event.status != EventStatus.PLANNED) {
                throw ConflictException(
                    "Only 'PLANNED' events can be edited. Current status is ${event.status}.",
                )
            }

            if (period != null && period.start < now()) {
                throw ValidationException("New event start date must be in the future.")
            }

            if (maxParticipants != null && maxParticipants < event.participantsIds.size) {
                throw ValidationException(
                    "Max participants cannot be less than the current number of participants.",
                )
            }

            val updatedEvent =
                event.copy(
                    title = title ?: event.title,
                    description = description ?: event.description,
                    period = period ?: event.period,
                    maxParticipants = maxParticipants ?: event.maxParticipants,
                )
            if (updatedEvent != event) eventRepository.update(updatedEvent) else event
        }

/*    suspend fun cancelEvent(
        eventId: Id,
        userId: Id,
    ): Result<Event> =
        runCatching {
            val event = findEventOrFail(eventId)
            ensureOrganizerOrFail(event, userId)

            if (event.status == EventStatus.COMPLETED || event.status == EventStatus.CANCELLED) {
                throw ConflictException("Event is already ${event.status} and cannot be cancelled.")
            }

            val cancelledEvent = event.copy(status = EventStatus.CANCELLED)
            val updatedEvent = eventRepository.update(cancelledEvent)

            val zone = findZoneOrFail(event.zoneId)
            if (zone.eventId == event.id) {
                val updatedZone = zone.copy(eventId = null, status = ZoneStatus.REPORTED)
                zoneRepository.update(updatedZone)
            }
            updatedEvent
        }

    suspend fun completeEvent(
        eventId: Id,
        actingUserId: Id,
    ): Result<Event> =
        runCatching {
            val event = findEventOrFail(eventId)
            ensureOrganizerOrFail(event, actingUserId)

            if (event.status != EventStatus.IN_PROGRESS) {
                throw ConflictException("Only 'IN_PROGRESS' events can be completed.")
            }

            val completedEvent = event.copy(status = EventStatus.COMPLETED)
            val updatedEvent = eventRepository.update(completedEvent)

            val zone = findZoneOrFail(event.zoneId)
            if (zone.eventId == event.id) {
                val updatedZone = zone.copy(eventId = null, status = ZoneStatus.CLEANED)
                zoneRepository.update(updatedZone)
            }
            updatedEvent
        }*/

    suspend fun joinEvent(
        eventId: Id,
        userId: Id,
    ): Result<Event> =
        runCatching {
            val event = findEventOrFail(eventId)
            findUserOrFail(userId)

            if (event.status !in listOf(EventStatus.PLANNED, EventStatus.IN_PROGRESS)) {
                throw ConflictException(
                    "Can only join events that are 'PLANNED' or 'IN_PROGRESS'. " +
                        "Current status: '${event.status}'."
                )
            }
            if (userId in event.participantsIds) {
                throw ConflictException("User '$userId' is already a participant in this event.")
            }
            if (event.isFull) {
                throw ConflictException(
                    "Event '$eventId' is full and cannot accept more participants.",
                )
            }

            val updatedEvent = event.copy(participantsIds = event.participantsIds + userId)
            eventRepository.update(updatedEvent)
        }

    suspend fun leaveEvent(
        eventId: Id,
        userId: Id,
    ): Result<Event> =
        runCatching {
            val event = findEventOrFail(eventId)
            findUserOrFail(userId)

            if (event.status != EventStatus.PLANNED) {
                throw ConflictException(
                    "Cannot leave an event that is not 'PLANNED'. " +
                        "Current status: ${event.status}.",
                )
            }
            if (userId == event.organizerId) {
                throw AuthorizationException(
                    "Organizer cannot leave their own event. They must cancel or delete it.",
                )
            }
            if (userId !in event.participantsIds) {
                throw NotFoundException("User '$userId' is not a participant in event '$eventId'.")
            }

            val updatedEvent = event.copy(participantsIds = event.participantsIds - userId)
            eventRepository.update(updatedEvent)
        }

    suspend fun getEventParticipants(eventId: Id): Result<List<User>> =
        runCatching {
            val event = findEventOrFail(eventId)
            event.participantsIds.mapNotNull { userRepository.getById(it) }
        }

    suspend fun deleteEvent(
        eventId: Id,
        userId: Id,
    ): Result<Unit> =
        runCatching {
            val event = findEventOrFail(eventId)
            ensureOrganizerOrFail(event, userId)

            if (event.status != EventStatus.PLANNED && event.status != EventStatus.CANCELLED) {
                throw ConflictException(
                    "Only 'PLANNED' or 'CANCELLED' events can be deleted. " +
                        "Current status: ${event.status}.",
                )
            }

            val zone = findZoneOrFail(event.zoneId)
            if (zone.eventId == event.id) {
                val updatedZone = zone.copy(eventId = null, status = ZoneStatus.REPORTED)
                zoneRepository.update(updatedZone)
            }

            messageRepository.deleteAllByEventId(eventId)

            val deleted = eventRepository.deleteById(eventId)
            if (!deleted) throw InternalServerException("Failed to delete event '$eventId'.")
            Unit
        }

    private suspend fun findUserOrFail(userId: Id): User =
        userRepository.getById(userId)
            ?: throw NotFoundException("User '$userId' not found.")

    private suspend fun findEventOrFail(eventId: Id): Event =
        eventRepository.getById(eventId)
            ?: throw NotFoundException("Event '$eventId' not found.")

    private suspend fun findZoneOrFail(zoneId: Id): Zone =
        zoneRepository.getById(zoneId)
            ?: throw NotFoundException("Zone '$zoneId' not found.")

    private fun ensureOrganizerOrFail(
        event: Event,
        userId: Id,
    ) {
        if (event.organizerId != userId) {
            throw AuthorizationException(
                "User '$userId' is not authorized. " +
                    "Must be the event organizer ('${event.organizerId}').",
            )
        }
    }

    suspend fun checkInUserToEvent(
        eventId: Id,
        userIdToCheckIn: Id,
        actingUserId: Id,
    ): Result<Unit> =
        runCatching {
            val event = findEventOrFail(eventId)

            ensureOrganizerOrFail(event, actingUserId)

            if (userIdToCheckIn == event.organizerId) {
                throw ConflictException("The organizer is automatically considered present and cannot be checked in manually.")
            }

            if (event.status != EventStatus.IN_PROGRESS) {
                throw ConflictException(
                    "Cannot check in user. Event must be 'IN_PROGRESS', but is '${event.status}'.",
                )
            }

            findUserOrFail(userIdToCheckIn)

            if (userIdToCheckIn !in event.participantsIds) {
                throw ValidationException(
                    "User '$userIdToCheckIn' is not registered for this event.",
                )
            }

            val alreadyAttended = eventRepository.hasAttended(eventId, userIdToCheckIn)
            if (alreadyAttended) {
                throw ConflictException(
                    "User '$userIdToCheckIn' has already checked in to this event.",
                )
            }

            eventRepository.addAttendance(
                eventId = eventId,
                userId = userIdToCheckIn,
                checkedInAt = now(),
            )
        }

    suspend fun getEventAttendees(eventId: Id): Result<List<User>> =
        runCatching {
            val event = findEventOrFail(eventId)
            val attendeeIdsFromDb  = eventRepository.getAttendeesIds(eventId)
            val finalAttendeeIds = if (event.status != EventStatus.PLANNED) { //Started or completed
                attendeeIdsFromDb + event.organizerId // Include organizer in attendees
            } else {
                attendeeIdsFromDb
            }
            finalAttendeeIds.distinct().mapNotNull { userRepository.getById(it) } //distinct to avoid duplicates
        }

    suspend fun getEventsAttendedBy(
        userId: Id,
        limit: Int,
        offset: Int,
    ): Result<List<Event>> =
        runCatching {
            findUserOrFail(userId)

            eventRepository.findEventsAttendedByUser(userId, limit, offset)
        }
}
