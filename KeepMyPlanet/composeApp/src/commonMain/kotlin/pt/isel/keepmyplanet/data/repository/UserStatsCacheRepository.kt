package pt.isel.keepmyplanet.data.repository

import kotlinx.datetime.Clock
import pt.isel.keepmyplanet.cache.KeepMyPlanetCache
import pt.isel.keepmyplanet.domain.common.Id
import pt.isel.keepmyplanet.domain.user.UserStats

class UserStatsCacheRepository(
    database: KeepMyPlanetCache,
) {
    private val queries = database.userStatsCacheQueries

    fun getStatsByUserId(userId: Id): UserStats? =
        queries.getStatsByUserId(userId.value.toLong()).executeAsOneOrNull()?.let {
            UserStats(
                totalEventsAttended = it.total_events_attended.toInt(),
                totalHoursVolunteered = it.total_hours_volunteered,
            )
        }

    suspend fun insertOrUpdateStats(
        userId: Id,
        stats: UserStats,
    ) {
        queries.insertOrUpdateStats(
            user_id = userId.value.toLong(),
            total_events_attended = stats.totalEventsAttended.toLong(),
            total_hours_volunteered = stats.totalHoursVolunteered,
            timestamp = Clock.System.now().epochSeconds,
        )
    }

    suspend fun deleteExpiredStats(ttlSeconds: Long) {
        val expirationTime = Clock.System.now().epochSeconds - ttlSeconds
        queries.deleteExpiredStats(expirationTime)
    }
}
