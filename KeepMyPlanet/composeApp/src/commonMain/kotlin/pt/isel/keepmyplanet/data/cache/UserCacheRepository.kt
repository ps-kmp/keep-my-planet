package pt.isel.keepmyplanet.data.cache

import kotlinx.datetime.Clock
import pt.isel.keepmyplanet.cache.KeepMyPlanetCache
import pt.isel.keepmyplanet.data.cache.mappers.toUserCacheInfo
import pt.isel.keepmyplanet.domain.common.Id
import pt.isel.keepmyplanet.domain.user.UserCacheInfo

class UserCacheRepository(
    database: KeepMyPlanetCache,
) : CleanableCache {
    private val queries = database.userCacheQueries

    suspend fun insertUsers(users: List<UserCacheInfo>) {
        queries.transaction {
            users.forEach { user ->
                queries.insertUser(
                    id = user.id.value.toLong(),
                    name = user.name.value,
                    email = user.email.value,
                    profilePictureId = user.profilePictureId?.value?.toLong(),
                    timestamp = Clock.System.now().epochSeconds,
                )
            }
        }
    }

    suspend fun getUserById(id: Id): UserCacheInfo? =
        queries.getUserById(id.value.toLong()).executeAsOneOrNull()?.toUserCacheInfo()

    suspend fun getUsersByIds(ids: List<Id>): List<UserCacheInfo> =
        queries
            .getUsersByIds(ids.map { it.value.toLong() })
            .executeAsList()
            .map { it.toUserCacheInfo() }

    override suspend fun cleanupExpiredData(ttlSeconds: Long) {
        val expirationTime = Clock.System.now().epochSeconds - ttlSeconds
        queries.deleteExpiredUsers(expirationTime)
    }
}
