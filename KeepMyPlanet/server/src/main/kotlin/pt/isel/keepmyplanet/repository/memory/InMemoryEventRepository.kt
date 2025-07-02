package pt.isel.keepmyplanet.repository.memory

import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.atomic.AtomicInteger
import kotlin.text.compareTo
import kotlinx.datetime.Clock
import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.plus
import kotlinx.datetime.toInstant
import kotlinx.datetime.toLocalDateTime
import pt.isel.keepmyplanet.domain.common.Description
import pt.isel.keepmyplanet.domain.common.Id
import pt.isel.keepmyplanet.domain.event.Event
import pt.isel.keepmyplanet.domain.event.EventStatus
import pt.isel.keepmyplanet.domain.event.Period
import pt.isel.keepmyplanet.domain.event.Title
import pt.isel.keepmyplanet.exception.NotFoundException
import pt.isel.keepmyplanet.repository.EventRepository
import pt.isel.keepmyplanet.utils.now

class InMemoryEventRepository : EventRepository {
    private val events = ConcurrentHashMap<Id, Event>()
    private val nextId = AtomicInteger(1)
    private val attendances = ConcurrentHashMap<Id, MutableMap<Id, LocalDateTime>>()

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
                title = Title("Beach Cleaning"),
                description = Description("Join us for a beach cleaning event!"),
                period = period,
                zoneId = Id(1U),
                organizerId = Id(2U),
                participantsIds = setOf(Id(1U), Id(2U), Id(3U)),
                createdAt = now(),
                updatedAt = now(),
            )
        val testEvent2 =
            Event(
                id = Id(2U),
                title = Title("Park Cleanup"),
                description = Description("Help us clean up the park!"),
                period = period,
                zoneId = Id(2U),
                organizerId = Id(2U),
                participantsIds = setOf(Id(1U), Id(2U), Id(3U)),
                createdAt = now(),
                updatedAt = now(),
            )
        val testEvent3 =
            Event(
                id = Id(3U),
                title = Title("City Cleanup"),
                description = Description("Join us for a city cleanup event!"),
                period = period,
                zoneId = Id(3U),
                organizerId = Id(3U),
                participantsIds = setOf(Id(1U), Id(2U), Id(3U)),
                createdAt = now(),
                updatedAt = now(),
            )
        events[testEvent1.id] = testEvent1
        events[testEvent2.id] = testEvent2
        events[testEvent3.id] = testEvent3
    }

    override suspend fun save(event: Event): Event =
        if (events.containsKey(event.id)) {
            update(event)
        } else {
            create(event)
        }

    override suspend fun findByName(
        name: String,
        limit: Int,
        offset: Int,
    ): List<Event> =
        events.values
            .filter { it.title.value.contains(name, ignoreCase = true) }
            .sortedByDescending { it.period.start }
            .drop(offset)
            .take(limit)

    override suspend fun findByZoneAndName(
        zoneId: Id,
        name: String,
    ): List<Event> =
        events.values
            .filter { it.zoneId == zoneId && it.title.value.contains(name, ignoreCase = true) }
            .sortedBy { it.period.start }

    override suspend fun addAttendance(
        eventId: Id,
        userId: Id,
        checkedInAt: LocalDateTime,
    ) {
        events[eventId] ?: throw NotFoundException("Event '$eventId' not found.")
        attendances.computeIfAbsent(eventId) { ConcurrentHashMap() }[userId] = checkedInAt
    }

    override suspend fun hasAttended(
        eventId: Id,
        userId: Id,
    ): Boolean = attendances[eventId]?.containsKey(userId) == true

    override suspend fun getAttendeesIds(eventId: Id): Set<Id> =
        attendances[eventId]?.keys?.toSet() ?: emptySet()

    override suspend fun findEventsAttendedByUser(
        userId: Id,
        limit: Int,
        offset: Int,
    ): List<Event> {
        val attendedEventIds = attendances.filter { it.value.containsKey(userId) }.keys
        return attendedEventIds
            .mapNotNull { events[it] }
            .sortedByDescending { it.period.start }
            .drop(offset)
            .take(limit)
    }

    override suspend fun findEventsToStart(): List<Event> {
        val currentTime = now()
        return events.values
            .filter { it.period.start <= currentTime && it.status == EventStatus.PLANNED }
            .sortedBy { it.period.start }
    }

    override suspend fun countAttendedEvents(userId: Id): Long {
        val attendedEventIds = attendances.filter { it.value.containsKey(userId) }.keys
        return events.values
            .count { it.id in attendedEventIds && it.status == EventStatus.COMPLETED }
            .toLong()
    }

    override suspend fun calculateTotalHoursVolunteered(userId: Id): Double {
        val attendedEventIds =
            attendances
                .filter { it.value.containsKey(userId) }
                .keys

        return events.values
            .filter { it.id in attendedEventIds }
            .filter { it.status == EventStatus.COMPLETED && it.period.end != null }
            .sumOf {
                (it.period.end!!.toInstant(TimeZone.UTC) - it.period.start.toInstant(TimeZone.UTC))
                    .inWholeSeconds
            }.toDouble()
    }

    override suspend fun findCompletedEventsPendingConfirmation(
        timeThreshold: LocalDateTime,
    ): List<Event> {
        return events.values
            .filter {
                it.status == EventStatus.COMPLETED && it.updatedAt < timeThreshold
            }
            .sortedBy { it.updatedAt }
    }

    override suspend fun create(entity: Event): Event {
        val newId = Id(nextId.getAndIncrement().toUInt())
        val currentTime = now()
        val newEvent = entity.copy(id = newId, createdAt = currentTime, updatedAt = currentTime)
        events[newId] = newEvent
        return newEvent
    }

    override suspend fun getById(id: Id): Event? = events[id]

    override suspend fun getAll(
        limit: Int,
        offset: Int,
    ): List<Event> =
        events.values
            .toList()
            .sortedByDescending { it.period.start }
            .drop(offset)
            .take(limit)

    override suspend fun update(entity: Event): Event {
        val event = events[entity.id] ?: throw NotFoundException("Event '${entity.id}' not found.")
        val updatedEvent = entity.copy(createdAt = event.createdAt, updatedAt = now())
        events[entity.id] = updatedEvent
        return updatedEvent
    }

    override suspend fun deleteById(id: Id): Boolean = events.remove(id) != null

    override suspend fun findByOrganizerId(
        organizerId: Id,
        limit: Int,
        offset: Int,
    ): List<Event> =
        events.values
            .filter { it.organizerId == organizerId }
            .sortedByDescending { it.period.start }
            .drop(offset)
            .take(limit)

    override suspend fun findByNameAndOrganizerId(
        organizerId: Id,
        name: String,
        limit: Int,
        offset: Int,
    ): List<Event> =
        events.values
            .filter {
                it.organizerId == organizerId &&
                    it.title.value.contains(name, ignoreCase = true)
            }.sortedByDescending { it.period.start }
            .drop(offset)
            .take(limit)

    override suspend fun findByParticipantId(
        participantId: Id,
        limit: Int,
        offset: Int,
    ): List<Event> =
        events.values
            .filter { it.participantsIds.contains(participantId) }
            .sortedByDescending { it.period.start }
            .drop(offset)
            .take(limit)

    override suspend fun findByNameAndParticipantId(
        participantId: Id,
        name: String,
        limit: Int,
        offset: Int,
    ): List<Event> =
        events.values
            .filter {
                it.participantsIds.contains(participantId) &&
                    it.title.value.contains(name, ignoreCase = true)
            }.sortedByDescending { it.period.start }
            .drop(offset)
            .take(limit)

    override suspend fun findByZoneId(zoneId: Id): List<Event> =
        events.values
            .filter { it.zoneId == zoneId }
            .sortedBy { it.period.start }

    override suspend fun findByStatus(status: EventStatus): List<Event> =
        events.values
            .filter { it.status == status }
            .sortedBy { it.period.start }

    override suspend fun findEventsByZoneIds(zoneIds: List<Id>): List<Event> =
        events.values
            .filter { it.zoneId in zoneIds }
            .sortedBy { it.period.start }

    fun clear() {
        events.clear()
        nextId.set(1)
    }
}
