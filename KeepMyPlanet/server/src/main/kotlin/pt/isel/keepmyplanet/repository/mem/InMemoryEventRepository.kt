package pt.isel.keepmyplanet.repository.mem

import kotlinx.datetime.Clock
import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.TimeZone
import kotlinx.datetime.plus
import kotlinx.datetime.toLocalDateTime
import pt.isel.keepmyplanet.core.NotFoundException
import pt.isel.keepmyplanet.domain.common.Description
import pt.isel.keepmyplanet.domain.common.Id
import pt.isel.keepmyplanet.domain.common.Location
import pt.isel.keepmyplanet.domain.common.Title
import pt.isel.keepmyplanet.domain.event.Event
import pt.isel.keepmyplanet.domain.event.EventStatus
import pt.isel.keepmyplanet.domain.event.Period
import pt.isel.keepmyplanet.repository.EventRepository
import pt.isel.keepmyplanet.repository.ZoneRepository
import pt.isel.keepmyplanet.util.calculateDistanceKm
import pt.isel.keepmyplanet.util.now
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.atomic.AtomicInteger

class InMemoryEventRepository(
    private val zoneRepository: ZoneRepository,
) : EventRepository {
    private val events = ConcurrentHashMap<Id, Event>()
    private val nextId = AtomicInteger(1)

    init {
        val testEvent =
            Event(
                id = Id(1U),
                title = Title("Limpeza Praia"),
                description = Description("Limpeza da Praia da Rocha"),
                period =
                    Period(
                        now(),
                        Clock.System
                            .now()
                            .plus(5, DateTimeUnit.HOUR)
                            .toLocalDateTime(TimeZone.UTC),
                    ),
                zoneId = Id(1U),
                organizerId = Id(1U),
                status = EventStatus.IN_PROGRESS,
                createdAt = now(),
            )
        events[testEvent.id] = testEvent
    }

    override suspend fun create(entity: Event): Event {
        val newId = Id(nextId.getAndIncrement().toUInt())
        val currentTime = now()
        val newEvent = entity.copy(id = newId, createdAt = currentTime, updatedAt = currentTime)
        events[newId] = newEvent
        return newEvent
    }

    override suspend fun getById(id: Id): Event? = events[id]

    override suspend fun getAll(): List<Event> =
        events.values
            .toList()
            .sortedBy { it.id.value }

    override suspend fun update(entity: Event): Event {
        val existingEvent = events[entity.id] ?: throw NotFoundException("Event", entity.id)
        val updatedEvent =
            entity.copy(
                participantsIds = existingEvent.participantsIds,
                createdAt = existingEvent.createdAt,
                updatedAt = now(),
            )
        events[updatedEvent.id] = updatedEvent
        return updatedEvent
    }

    override suspend fun deleteById(id: Id): Boolean = events.remove(id) != null

    override suspend fun findByZoneId(zoneId: Id): List<Event> =
        events.values
            .filter { it.zoneId == zoneId }
            .sortedBy { it.period.start }

    override suspend fun findByOrganizerId(organizerId: Id): List<Event> =
        events.values
            .filter { it.organizerId == organizerId }
            .sortedBy { it.period.start }

    override suspend fun findByParticipantId(participantId: Id): List<Event> =
        events.values
            .filter { it.participantsIds.contains(participantId) }
            .sortedBy { it.period.start }

    override suspend fun findByStatus(status: EventStatus): List<Event> =
        events.values
            .filter { it.status == status }
            .sortedBy { it.period.start }

    override suspend fun findNearLocation(
        center: Location,
        radiusKm: Double,
    ): List<Event> {
        val eventsWithDistance = mutableListOf<Pair<Event, Double>>()
        for (event in events.values) {
            val zone = zoneRepository.getById(event.zoneId)
            if (zone != null) {
                val distance = calculateDistanceKm(zone.location, center)
                if (distance <= radiusKm) eventsWithDistance.add(event to distance)
            }
        }
        return eventsWithDistance
            .sortedBy { it.second }
            .map { it.first }
    }

    fun clear() {
        events.clear()
        nextId.set(1)
    }
}
