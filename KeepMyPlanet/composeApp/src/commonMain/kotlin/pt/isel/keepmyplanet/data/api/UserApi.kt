package pt.isel.keepmyplanet.data.api

import io.ktor.client.HttpClient
import io.ktor.client.request.setBody
import io.ktor.client.request.url
import io.ktor.http.HttpMethod
import pt.isel.keepmyplanet.data.http.executeRequest
import pt.isel.keepmyplanet.data.http.executeRequestUnit
import pt.isel.keepmyplanet.dto.auth.ChangePasswordRequest
import pt.isel.keepmyplanet.dto.auth.RegisterRequest
import pt.isel.keepmyplanet.dto.user.UpdateProfileRequest
import pt.isel.keepmyplanet.dto.user.UserResponse
import pt.isel.keepmyplanet.dto.user.UserStatsResponse

class UserApi(
    private val httpClient: HttpClient,
) {
    private object Endpoints {
        const val USERS_BASE = "users"

        fun registerUser() = USERS_BASE

        fun getAllUsers() = USERS_BASE

        fun userById(userId: UInt) = "$USERS_BASE/$userId"

        fun updateUser(userId: UInt) = userById(userId)

        fun deleteUser(userId: UInt) = userById(userId)

        fun getUserStats(userId: UInt) = "${userById(userId)}/stats"

        fun changePassword(userId: UInt) = "${userById(userId)}/password"
    }

    suspend fun registerUser(request: RegisterRequest): Result<UserResponse> =
        httpClient.executeRequest {
            method = HttpMethod.Post
            url(Endpoints.registerUser())
            setBody(request)
        }

    suspend fun getAllUsers(): Result<List<UserResponse>> =
        httpClient.executeRequest {
            method = HttpMethod.Get
            url(Endpoints.getAllUsers())
        }

    suspend fun getUserDetails(userId: UInt): Result<UserResponse> =
        httpClient.executeRequest {
            method = HttpMethod.Get
            url(Endpoints.userById(userId))
        }

    suspend fun updateUserProfile(
        userIdToUpdate: UInt,
        request: UpdateProfileRequest,
    ): Result<UserResponse> =
        httpClient.executeRequest {
            method = HttpMethod.Patch
            url(Endpoints.updateUser(userIdToUpdate))
            setBody(request)
        }

    suspend fun deleteUser(userIdToDelete: UInt): Result<Unit> =
        httpClient.executeRequestUnit {
            method = HttpMethod.Delete
            url(Endpoints.deleteUser(userIdToDelete))
        }

    suspend fun changePassword(
        userIdToUpdate: UInt,
        request: ChangePasswordRequest,
    ): Result<Unit> =
        httpClient.executeRequestUnit {
            method = HttpMethod.Patch
            url(Endpoints.changePassword(userIdToUpdate))
            setBody(request)
        }

    suspend fun getUserStats(userId: UInt): Result<UserStatsResponse> =
        httpClient.executeRequest {
            method = HttpMethod.Get
            url(Endpoints.getUserStats(userId))
        }
}
