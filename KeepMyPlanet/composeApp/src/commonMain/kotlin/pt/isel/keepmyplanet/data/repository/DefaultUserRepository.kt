package pt.isel.keepmyplanet.data.repository

import pt.isel.keepmyplanet.data.api.UserApi
import pt.isel.keepmyplanet.data.cache.UserCacheRepository
import pt.isel.keepmyplanet.data.cache.UserStatsCacheRepository
import pt.isel.keepmyplanet.domain.common.Id
import pt.isel.keepmyplanet.domain.user.UserInfo
import pt.isel.keepmyplanet.domain.user.UserStats
import pt.isel.keepmyplanet.dto.auth.ChangePasswordRequest
import pt.isel.keepmyplanet.dto.auth.RegisterRequest
import pt.isel.keepmyplanet.dto.user.UpdateProfileRequest
import pt.isel.keepmyplanet.mapper.user.toDomain
import pt.isel.keepmyplanet.mapper.user.toUserCacheInfo
import pt.isel.keepmyplanet.mapper.user.toUserInfo

class DefaultUserRepository(
    private val userApi: UserApi,
    private val userCache: UserCacheRepository,
    private val userStatsCache: UserStatsCacheRepository,
) {
    suspend fun registerUser(request: RegisterRequest): Result<UserInfo> =
        userApi.registerUser(request).map { it.toUserInfo() }

    suspend fun getUserDetails(userId: Id): Result<UserInfo> =
        runCatching {
            val networkResult = userApi.getUserDetails(userId.value)
            if (networkResult.isSuccess) {
                val userInfo = networkResult.getOrThrow().toUserInfo()
                userCache.insertUsers(listOf(userInfo.toUserCacheInfo()))
                userInfo
            } else {
                userCache.getUserById(userId)?.toUserInfo()
                    ?: throw networkResult.exceptionOrNull()!!
            }
        }

    suspend fun updateUserProfile(
        userIdToUpdate: Id,
        request: UpdateProfileRequest,
    ): Result<UserInfo> =
        userApi.updateUserProfile(userIdToUpdate.value, request).map {
            val userInfo = it.toUserInfo()
            userCache.insertUsers(listOf(userInfo.toUserCacheInfo()))
            userInfo
        }

    suspend fun changePassword(
        userIdToUpdate: Id,
        request: ChangePasswordRequest,
    ): Result<Unit> = userApi.changePassword(userIdToUpdate.value, request)

    suspend fun deleteUser(userIdToDelete: Id): Result<Unit> =
        userApi.deleteUser(userIdToDelete.value)

    suspend fun getUserStats(userId: Id): Result<UserStats> =
        runCatching {
            val networkResult = userApi.getUserStats(userId.value)
            if (networkResult.isSuccess) {
                val stats = networkResult.getOrThrow().toDomain()
                userStatsCache.insertOrUpdateStats(userId, stats)
                stats
            } else {
                userStatsCache.getStatsByUserId(userId) ?: throw networkResult.exceptionOrNull()!!
            }
        }
}
