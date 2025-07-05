package pt.isel.keepmyplanet.data.cache

import kotlinx.datetime.Clock
import pt.isel.keepmyplanet.cache.KeepMyPlanetCache
import pt.isel.keepmyplanet.data.cache.mappers.toResponse
import pt.isel.keepmyplanet.domain.common.Id
import pt.isel.keepmyplanet.dto.event.EventStateChangeResponse

class EventStatusHistoryCacheRepository(
    database: KeepMyPlanetCache,
) : CleanableCache {
    private val queries = database.eventStatusHistoryCacheQueries

    fun getHistoryByEventId(eventId: Id): List<EventStateChangeResponse> =
        queries.getHistoryByEventId(eventId.value.toLong()).executeAsList().map { it.toResponse() }

    suspend fun insertHistory(
        eventId: Id,
        history: List<EventStateChangeResponse>,
    ) {
        queries.transaction {
            history.forEach {
                queries.insertHistory(
                    id = it.id.toLong(),
                    event_id = eventId.value.toLong(),
                    new_status = it.newStatus.name,
                    changed_by_id = it.changedBy.id.toLong(),
                    changed_by_name = it.changedBy.name,
                    change_time = it.changeTime,
                    timestamp = Clock.System.now().epochSeconds,
                )
            }
        }
    }

    override suspend fun cleanupExpiredData(ttlSeconds: Long) {
        val expirationTime = Clock.System.now().epochSeconds - ttlSeconds
        queries.deleteExpiredHistory(expirationTime)
    }
}
