package pt.isel.keepmyplanet.data.cache

import kotlinx.datetime.Clock
import pt.isel.keepmyplanet.cache.KeepMyPlanetCache
import pt.isel.keepmyplanet.domain.common.Id
import pt.isel.keepmyplanet.domain.event.EventStats

class EventStatsCacheRepository(
    database: KeepMyPlanetCache,
) : CleanableCache {
    private val queries = database.eventStatsCacheQueries

    suspend fun getStatsByEventId(eventId: Id): EventStats? =
        queries.getStatsByEventId(eventId.value.toLong()).executeAsOneOrNull()?.let {
            EventStats(
                totalParticipants = it.total_participants.toInt(),
                totalAttendees = it.total_attendees.toInt(),
                checkInRate = it.check_in_rate,
                totalHoursVolunteered = it.total_hours_volunteered,
            )
        }

    suspend fun insertOrUpdateStats(
        eventId: Id,
        stats: EventStats,
    ) {
        queries.insertOrUpdateStats(
            event_id = eventId.value.toLong(),
            total_participants = stats.totalParticipants.toLong(),
            total_attendees = stats.totalAttendees.toLong(),
            check_in_rate = stats.checkInRate,
            total_hours_volunteered = stats.totalHoursVolunteered,
            timestamp = Clock.System.now().epochSeconds,
        )
    }

    override suspend fun cleanupExpiredData(ttlSeconds: Long) {
        val expirationTime = Clock.System.now().epochSeconds - ttlSeconds
        queries.deleteExpiredStats(expirationTime)
    }
}
