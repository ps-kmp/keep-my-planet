package pt.isel.keepmyplanet.repository.mem

import kotlinx.datetime.Clock
import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.TimeZone
import kotlinx.datetime.plus
import kotlinx.datetime.toLocalDateTime
import pt.isel.keepmyplanet.domain.common.Description
import pt.isel.keepmyplanet.domain.common.Id
import pt.isel.keepmyplanet.domain.event.Event
import pt.isel.keepmyplanet.domain.event.EventStatus
import pt.isel.keepmyplanet.domain.event.Period
import pt.isel.keepmyplanet.domain.event.Title
import pt.isel.keepmyplanet.errors.NotFoundException
import pt.isel.keepmyplanet.repository.EventRepository
import pt.isel.keepmyplanet.repository.ZoneRepository
import pt.isel.keepmyplanet.util.now
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.atomic.AtomicInteger

class InMemoryEventRepository(
    private val zoneRepository: ZoneRepository,
) : EventRepository {
    private val events = ConcurrentHashMap<Id, Event>() // Id of the event - Event
    private val nextId = AtomicInteger(1)

    init {
        val period =
            Period(
                now(),
                Clock.System
                    .now()
                    .plus(5, DateTimeUnit.HOUR)
                    .toLocalDateTime(TimeZone.UTC),
            )
        val testEvent1 =
            Event(
                id = Id(1U),
                title = Title("e1"),
                description = Description("d1"),
                period = period,
                zoneId = Id(1U),
                organizerId = Id(2U),
                participantsIds = setOf(Id(1U), Id(2U), Id(3U)),
                createdAt = now(),
            )
        val testEvent2 =
            Event(
                id = Id(2U),
                title = Title("e2"),
                description = Description("d2"),
                period = period,
                zoneId = Id(2U),
                organizerId = Id(2U),
                participantsIds = setOf(Id(1U), Id(2U), Id(3U)),
                createdAt = now(),
            )
        val testEvent3 =
            Event(
                id = Id(3U),
                title = Title("e3"),
                description = Description("d3"),
                period = period,
                zoneId = Id(3U),
                organizerId = Id(3U),
                participantsIds = setOf(Id(1U), Id(2U), Id(3U)),
                createdAt = now(),
            )
        events[testEvent1.id] = testEvent1
        events[testEvent2.id] = testEvent2
        events[testEvent3.id] = testEvent3
    }

    override suspend fun save(event: Event): Event = if (events.containsKey(event.id)) update(event) else create(event)

    // Get all events from the system (search by name)
    override suspend fun findByName(name: String): List<Event> =
        events.values
            .filter {
                it.title.value.contains(name, ignoreCase = true)
            }.sortedBy { it.period.start }

    override suspend fun findByZoneId(zoneId: Id): List<Event> = events.values.filter { it.zoneId == zoneId }

    override suspend fun findByZoneAndName(
        zoneId: Id,
        name: String,
    ): List<Event> =
        events.values
            .filter { it.zoneId == zoneId && it.title.value.contains(name, ignoreCase = true) }
            .sortedBy { it.period.start }

    override suspend fun create(entity: Event): Event {
        val newId = Id(nextId.getAndIncrement().toUInt())
        val currentTime = now()
        val newEvent = entity.copy(id = newId, createdAt = currentTime, updatedAt = currentTime)
        events[newId] = newEvent
        return newEvent
    }

    override suspend fun getById(id: Id): Event? = events[id]

    // Get all events from the system
    override suspend fun getAll(): List<Event> =
        events.values
            .toList()
            .sortedBy { it.id.value }

    override suspend fun update(entity: Event): Event {
        val existingEvent =
            events[entity.id] ?: throw NotFoundException("Event '${entity.id}' not found.")
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

    /*override suspend fun findByZoneId(zoneId: Id): List<Event> =
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
    }*/

    fun clear() {
        events.clear()
        nextId.set(1)
    }
}
