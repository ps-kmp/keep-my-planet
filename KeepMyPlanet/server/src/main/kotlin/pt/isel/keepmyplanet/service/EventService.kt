package pt.isel.keepmyplanet.service

import kotlinx.datetime.LocalDateTime
import pt.isel.keepmyplanet.domain.common.Description
import pt.isel.keepmyplanet.domain.common.Id
import pt.isel.keepmyplanet.domain.event.Event
import pt.isel.keepmyplanet.domain.event.EventStatus
import pt.isel.keepmyplanet.domain.event.Period
import pt.isel.keepmyplanet.domain.event.Title
import pt.isel.keepmyplanet.domain.zone.Zone
import pt.isel.keepmyplanet.errors.ConflictException
import pt.isel.keepmyplanet.errors.NotFoundException
import pt.isel.keepmyplanet.repository.EventRepository
import pt.isel.keepmyplanet.repository.ZoneRepository
import pt.isel.keepmyplanet.util.now

class EventService(
    private val eventRepository: EventRepository,
    private val zoneRepository: ZoneRepository,
) {
    suspend fun createEvent(
        zoneId: Id,
        organizerId: Id,
        title: String,
        description: String,
        periodStart: LocalDateTime,
        periodEnd: LocalDateTime,
        maxParticipants: Int?,
    ): Result<Event> =
        runCatching {
            val zone = findZoneOrFail(zoneId)
            val event =
                Event(
                    id = Id(0u),
                    zoneId = zone.id,
                    title = Title(title),
                    description = Description(description),
                    organizerId = organizerId,
                    period = Period(start = periodStart, end = periodEnd),
                    maxParticipants = maxParticipants,
                    status = EventStatus.PLANNED,
                    createdAt = now(),
                )

            eventRepository.create(event)
        }

    suspend fun searchZoneEvents(
        zoneId: Id,
        name: String?,
    ): Result<List<Event>> =
        runCatching {
            if (name.isNullOrBlank()) {
                eventRepository.findByZoneId(zoneId)
            } else {
                eventRepository.findByZoneAndName(zoneId, name)
            }
        }

    suspend fun searchAllEvents(name: String?): Result<List<Event>> =
        runCatching {
            if (name.isNullOrBlank()) {
                eventRepository.getAll()
            } else {
                eventRepository.findByName(name)
            }
        }

    suspend fun getEventDetails(eventId: Id): Result<Event> =
        runCatching {
            findEventOrFail(eventId)
        }

    suspend fun joinEvent(
        eventId: Id,
        userId: Id,
    ): Result<Unit> =
        runCatching {
            val event = findEventOrFail(eventId)

            if (event.status != EventStatus.PLANNED) {
                throw ConflictException("Cannot join event with status '${event.status}'.")
            }

            if (event.maxParticipants != null &&
                event.participantsIds.size >= event.maxParticipants!!
            ) {
                throw ConflictException("Maximum number of participants reached.")
            }

            if (userId == event.organizerId || userId in event.participantsIds) {
                throw ConflictException("User is already in the event.")
            }

            val updatedEvent =
                event.copy(
                    participantsIds = event.participantsIds + userId,
                    updatedAt = now(),
                )

            eventRepository.save(updatedEvent)
        }

    private suspend fun findEventOrFail(eventId: Id): Event =
        eventRepository.getById(eventId)
            ?: throw NotFoundException("Event '$eventId' not found.")

    private suspend fun findZoneOrFail(zoneId: Id): Zone =
        zoneRepository.getById(zoneId)
            ?: throw NotFoundException("Zone '$zoneId' not found.")
}
