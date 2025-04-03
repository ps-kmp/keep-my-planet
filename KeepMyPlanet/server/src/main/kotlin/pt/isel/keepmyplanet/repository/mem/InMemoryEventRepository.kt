package pt.isel.keepmyplanet.repository.mem

import kotlinx.datetime.LocalDateTime
import pt.isel.keepmyplanet.domain.common.Id
import pt.isel.keepmyplanet.domain.common.Location
import pt.isel.keepmyplanet.domain.event.Event
import pt.isel.keepmyplanet.domain.event.EventStatus
import pt.isel.keepmyplanet.repository.EventRepository
import pt.isel.keepmyplanet.services.EventNotFoundException
import pt.isel.keepmyplanet.services.MaxParticipantsReachedException
import pt.isel.keepmyplanet.services.ParticipantAlreadyRegisteredException
import pt.isel.keepmyplanet.services.ParticipantNotRegisteredException
import pt.isel.keepmyplanet.utils.nowUTC
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.atomic.AtomicInteger

class InMemoryEventRepository : EventRepository {
    private val eventsById = ConcurrentHashMap<Id, Event>()
    private val idCounter = AtomicInteger(1)

    private fun generateNewId(): Id = Id(idCounter.getAndIncrement().toUInt())

    override suspend fun create(entity: Event): Event {
        val now = LocalDateTime.nowUTC
        val eventToCreate =
            entity.copy(
                id = generateNewId(),
                participantsIds = emptySet(),
                createdAt = now,
                updatedAt = now,
            )
        eventsById[eventToCreate.id] = eventToCreate
        return eventToCreate
    }

    override suspend fun getById(id: Id): Event? = eventsById[id]

    override suspend fun getAll(): List<Event> = eventsById.values.toList()

    override suspend fun update(entity: Event): Event {
        val existingEvent = eventsById[entity.id] ?: throw EventNotFoundException(entity.id)
        if (entity.participantsIds != existingEvent.participantsIds) {
            println("Participant list was not updated. Use addParticipant/removeParticipant.")
        }
        val updatedEvent =
            entity.copy(
                participantsIds = existingEvent.participantsIds,
                createdAt = existingEvent.createdAt,
                updatedAt = LocalDateTime.nowUTC,
            )
        eventsById[updatedEvent.id] = updatedEvent
        return updatedEvent
    }

    override suspend fun deleteById(id: Id): Boolean = eventsById.remove(id) != null

    override suspend fun findByZoneId(zoneId: Id): List<Event> =
        eventsById.values
            .filter { it.zoneId == zoneId }
            .toList()

    override suspend fun findByOrganizerId(organizerId: Id): List<Event> =
        eventsById.values.filter { it.organizerId == organizerId }.toList()

    override suspend fun findByParticipantId(participantId: Id): List<Event> =
        eventsById.values.filter { it.participantsIds.contains(participantId) }.toList()

    override suspend fun addParticipant(
        eventId: Id,
        participantId: Id,
    ): Event {
        val event = eventsById[eventId] ?: throw EventNotFoundException(eventId)
        if (event.participantsIds.contains(participantId)) {
            throw ParticipantAlreadyRegisteredException(eventId, participantId)
        }
        event.maxParticipants?.let { max ->
            if (event.participantsIds.size >= max) {
                throw MaxParticipantsReachedException(eventId)
            }
        }
        val updatedEvent =
            event.copy(
                participantsIds = event.participantsIds + participantId,
                updatedAt = LocalDateTime.nowUTC,
            )
        eventsById[eventId] = updatedEvent
        return updatedEvent
    }

    override suspend fun removeParticipant(
        eventId: Id,
        participantId: Id,
    ): Event {
        val event = eventsById[eventId] ?: throw EventNotFoundException(eventId)
        if (!event.participantsIds.contains(participantId)) {
            throw ParticipantNotRegisteredException(eventId, participantId)
        }
        val updatedEvent =
            event.copy(
                participantsIds = event.participantsIds - participantId,
                updatedAt = LocalDateTime.nowUTC,
            )
        eventsById[eventId] = updatedEvent
        return updatedEvent
    }

    override suspend fun findEvents(
        center: Location?,
        radius: Double?,
        fromDate: LocalDateTime?,
        toDate: LocalDateTime?,
        statuses: List<EventStatus>?,
    ): List<Event> {
        if (center != null || radius != null) {
            println("Location filtering (center, radius) is not supported yet.")
        }
        return eventsById.values
            .filter { event ->
                val statusMatch =
                    statuses == null || statuses.isEmpty() || statuses.contains(event.status)
                val startDateMatch = fromDate == null || event.period.start >= fromDate
                val endDateMatch = toDate == null || event.period.end <= toDate
                // val locationMatch =

                statusMatch && startDateMatch && endDateMatch
                // && locationMatch
            }.toList()
    }

    override suspend fun isUserRegistered(
        eventId: Id,
        userId: Id,
    ): Boolean = eventsById[eventId]?.participantsIds?.contains(userId) == true

    fun clear() {
        eventsById.clear()
        idCounter.set(1)
    }
}
