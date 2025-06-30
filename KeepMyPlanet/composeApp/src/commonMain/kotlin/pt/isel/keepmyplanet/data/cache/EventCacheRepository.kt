package pt.isel.keepmyplanet.data.cache

import kotlinx.datetime.Clock
import kotlinx.serialization.json.Json
import pt.isel.keepmyplanet.cache.KeepMyPlanetCache
import pt.isel.keepmyplanet.data.cache.mappers.toEvent
import pt.isel.keepmyplanet.domain.common.Id
import pt.isel.keepmyplanet.domain.event.Event

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
                        Json.encodeToString(event.participantsIds.map { it.value }),
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
