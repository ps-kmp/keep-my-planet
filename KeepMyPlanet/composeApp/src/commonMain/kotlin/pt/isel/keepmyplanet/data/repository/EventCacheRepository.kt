package pt.isel.keepmyplanet.data.repository

import kotlinx.datetime.Clock
import kotlinx.datetime.LocalDateTime
import kotlinx.serialization.json.Json
import pt.isel.keepmyplanet.cache.EventCache
import pt.isel.keepmyplanet.cache.KeepMyPlanetCache
import pt.isel.keepmyplanet.domain.common.Description
import pt.isel.keepmyplanet.domain.common.Id
import pt.isel.keepmyplanet.domain.event.Event
import pt.isel.keepmyplanet.domain.event.EventStatus
import pt.isel.keepmyplanet.domain.event.Period
import pt.isel.keepmyplanet.domain.event.Title
import pt.isel.keepmyplanet.utils.safeValueOf

fun EventCache.toEvent(): Event {
    val participantIds =
        Json
            .decodeFromString<Set<UInt>>(this.participantIds_json)
            .map {
                Id(it)
            }.toSet()
    return Event(
        id = Id(this.id.toUInt()),
        title = Title(this.title),
        description = Description(this.description),
        period =
            Period(
                LocalDateTime.parse(this.startDate),
                this.endDate?.let { LocalDateTime.parse(it) },
            ),
        zoneId = Id(this.zoneId.toUInt()),
        organizerId = Id(this.organizerId.toUInt()),
        status = safeValueOf<EventStatus>(this.status) ?: EventStatus.UNKNOWN,
        maxParticipants = this.maxParticipants?.toInt(),
        participantsIds = participantIds,
        createdAt = LocalDateTime.parse(this.createdAt),
        updatedAt = LocalDateTime.parse(this.updatedAt),
    )
}

class EventCacheRepository(
    database: KeepMyPlanetCache,
) {
    private val queries = database.eventCacheQueries

    suspend fun insertEvents(events: List<Event>) {
        queries.transaction {
            events.forEach { event ->
                queries.insertEvent(
                    id = event.id.value.toLong(),
                    title = event.title.value,
                    description = event.description.value,
                    startDate = event.period.start.toString(),
                    endDate = event.period.end?.toString(),
                    zoneId = event.zoneId.value.toLong(),
                    organizerId = event.organizerId.value.toLong(),
                    status = event.status.name,
                    maxParticipants = event.maxParticipants?.toLong(),
                    participantIds_json =
                        Json.encodeToString(
                            event.participantsIds.map { it.value },
                        ),
                    createdAt = event.createdAt.toString(),
                    updatedAt = event.updatedAt.toString(),
                    timestamp = Clock.System.now().epochSeconds,
                )
            }
        }
    }

    fun getEventById(id: Id): Event? =
        queries.getEventById(id.value.toLong()).executeAsOneOrNull()?.toEvent()

    fun getAllEvents(): List<Event> = queries.getAllEvents().executeAsList().map { it.toEvent() }

    suspend fun deleteEventById(id: Id) {
        queries.deleteEventById(id.value.toLong())
    }

    suspend fun deleteExpiredEvents(ttlSeconds: Long) {
        val expirationTime = Clock.System.now().epochSeconds - ttlSeconds
        queries.deleteExpiredEvents(expirationTime)
    }
}
