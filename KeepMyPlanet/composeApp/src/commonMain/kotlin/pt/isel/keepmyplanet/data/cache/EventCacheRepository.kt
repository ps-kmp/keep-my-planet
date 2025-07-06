package pt.isel.keepmyplanet.data.cache

import kotlinx.datetime.Clock
import pt.isel.keepmyplanet.cache.KeepMyPlanetCache
import pt.isel.keepmyplanet.data.cache.mappers.toEvent
import pt.isel.keepmyplanet.domain.common.Id
import pt.isel.keepmyplanet.domain.event.Event

class EventCacheRepository(
    database: KeepMyPlanetCache,
) : CleanableCache {
    private val queries = database.eventCacheQueries
    private val participantQueries = database.eventParticipantsCacheQueries

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
                    pendingOrganizerId = event.pendingOrganizerId?.value?.toLong(),
                    transferRequestTime = event.transferRequestTime?.toString(),
                    createdAt = event.createdAt.toString(),
                    updatedAt = event.updatedAt.toString(),
                    timestamp = Clock.System.now().epochSeconds,
                )
                event.participantsIds.forEach { userId ->
                    participantQueries.insertParticipant(
                        event.id.value.toLong(),
                        userId.value.toLong(),
                    )
                }
            }
        }
    }

    suspend fun getEventById(id: Id): Event? {
        val dbEvent = queries.getEventById(id.value.toLong()).executeAsOneOrNull() ?: return null
        val participantIds =
            participantQueries
                .getParticipantsByEventId(id.value.toLong())
                .executeAsList()
                .map { Id(it.toUInt()) }
                .toSet()
        return dbEvent.toEvent(participantIds)
    }

    suspend fun getAllEvents(): List<Event> =
        queries.getAllEvents().executeAsList().map { dbEvent ->
            val participantIds =
                participantQueries
                    .getParticipantsByEventId(dbEvent.id)
                    .executeAsList()
                    .map { Id(it.toUInt()) }
                    .toSet()
            dbEvent.toEvent(participantIds)
        }

    suspend fun deleteEventById(id: Id) {
        queries.transaction {
            participantQueries.deleteParticipantsByEventId(id.value.toLong())
            queries.deleteEventById(id.value.toLong())
        }
    }

    override suspend fun cleanupExpiredData(ttlSeconds: Long) {
        val expirationTime = Clock.System.now().epochSeconds - ttlSeconds
        queries.deleteExpiredEvents(expirationTime)
    }
}
