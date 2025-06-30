package pt.isel.keepmyplanet.data.repository

import kotlinx.datetime.Clock
import pt.isel.keepmyplanet.cache.EventStatusHistoryCache
import pt.isel.keepmyplanet.cache.KeepMyPlanetCache
import pt.isel.keepmyplanet.domain.common.Id
import pt.isel.keepmyplanet.domain.event.EventStatus
import pt.isel.keepmyplanet.dto.event.EventStateChangeResponse
import pt.isel.keepmyplanet.dto.user.UserInfoSummaryResponse
import pt.isel.keepmyplanet.utils.safeValueOf

fun EventStatusHistoryCache.toResponse(): EventStateChangeResponse =
    EventStateChangeResponse(
        id = this.id.toUInt(),
        eventId = this.event_id.toUInt(),
        newStatus = safeValueOf<EventStatus>(this.new_status) ?: EventStatus.UNKNOWN,
        changedBy =
            UserInfoSummaryResponse(
                id = this.changed_by_id.toUInt(),
                name = this.changed_by_name,
            ),
        changeTime = this.change_time,
    )

class EventStatusHistoryCacheRepository(
    database: KeepMyPlanetCache,
) {
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

    suspend fun deleteExpiredHistory(ttlSeconds: Long) {
        val expirationTime = Clock.System.now().epochSeconds - ttlSeconds
        queries.deleteExpiredHistory(expirationTime)
    }
}
