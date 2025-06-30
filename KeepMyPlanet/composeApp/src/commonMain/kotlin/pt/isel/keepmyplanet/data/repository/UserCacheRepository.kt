package pt.isel.keepmyplanet.data.repository

import kotlinx.datetime.Clock
import pt.isel.keepmyplanet.cache.KeepMyPlanetCache
import pt.isel.keepmyplanet.cache.UserCache
import pt.isel.keepmyplanet.domain.common.Id
import pt.isel.keepmyplanet.domain.user.Email
import pt.isel.keepmyplanet.domain.user.Name
import pt.isel.keepmyplanet.domain.user.UserInfo

data class UserCacheInfo(
    val id: Id,
    val name: Name,
    val email: Email,
    val profilePictureUrl: String?,
)

fun UserCache.toUserCacheInfo(): UserCacheInfo =
    UserCacheInfo(
        id = Id(this.id.toUInt()),
        name = Name(this.name),
        email = Email(this.email),
        profilePictureUrl = this.profilePictureUrl,
    )

fun UserInfo.toUserCacheInfo(): UserCacheInfo =
    UserCacheInfo(
        id = this.id,
        name = this.name,
        email = this.email,
        profilePictureUrl = null,
    )

class UserCacheRepository(
    database: KeepMyPlanetCache,
) {
    private val queries = database.userCacheQueries

    suspend fun insertUsers(users: List<UserCacheInfo>) {
        queries.transaction {
            users.forEach { user ->
                queries.insertUser(
                    id = user.id.value.toLong(),
                    name = user.name.value,
                    email = user.email.value,
                    profilePictureUrl = user.profilePictureUrl,
                    timestamp = Clock.System.now().epochSeconds,
                )
            }
        }
    }

    fun getUserById(id: Id): UserCacheInfo? =
        queries.getUserById(id.value.toLong()).executeAsOneOrNull()?.toUserCacheInfo()

    fun getUsersByIds(ids: List<Id>): List<UserCacheInfo> =
        queries
            .getUsersByIds(ids.map { it.value.toLong() })
            .executeAsList()
            .map { it.toUserCacheInfo() }

    suspend fun deleteExpiredUsers(ttlSeconds: Long) {
        val expirationTime = Clock.System.now().epochSeconds - ttlSeconds
        queries.deleteExpiredUsers(expirationTime)
    }
}

fun UserCacheInfo.toUserInfo(): UserInfo =
    UserInfo(
        id = this.id,
        name = this.name,
        email = this.email,
        profilePictureId = null,
    )
