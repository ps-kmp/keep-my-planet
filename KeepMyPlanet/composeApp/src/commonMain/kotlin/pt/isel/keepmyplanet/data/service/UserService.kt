package pt.isel.keepmyplanet.data.service

import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.delete
import io.ktor.client.request.get
import io.ktor.client.request.header
import io.ktor.client.request.patch
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.contentType
import pt.isel.keepmyplanet.dto.user.ChangePasswordRequest
import pt.isel.keepmyplanet.dto.user.RegisterRequest
import pt.isel.keepmyplanet.dto.user.UpdateProfileRequest
import pt.isel.keepmyplanet.dto.user.UserResponse

class UserService(
    private val httpClient: HttpClient,
) {
    private object Endpoints {
        const val USERS_BASE = "users"

        fun registerUser() = USERS_BASE

        fun getAllUsers() = USERS_BASE

        fun userById(userId: UInt) = "$USERS_BASE/$userId"

        fun updateUser(userId: UInt) = userById(userId)

        fun deleteUser(userId: UInt) = userById(userId)

        fun changePassword(userId: UInt) = "${userById(userId)}/password"
    }

    suspend fun registerUser(request: RegisterRequest): Result<UserResponse> =
        runCatching {
            httpClient
                .post(Endpoints.registerUser()) {
                    contentType(ContentType.Application.Json)
                    setBody(request)
                }.body<UserResponse>()
        }

    suspend fun getAllUsers(): Result<List<UserResponse>> =
        runCatching {
            httpClient.get(Endpoints.getAllUsers()).body<List<UserResponse>>()
        }

    suspend fun getUserDetails(userId: UInt): Result<UserResponse> =
        runCatching {
            httpClient.get(Endpoints.userById(userId)).body<UserResponse>()
        }

    suspend fun updateUserProfile(
        userIdToUpdate: UInt,
        actingUserId: UInt,
        request: UpdateProfileRequest,
    ): Result<UserResponse> =
        runCatching {
            httpClient
                .patch(Endpoints.updateUser(userIdToUpdate)) {
                    contentType(ContentType.Application.Json)
                    setBody(request)
                    header("X-Mock-User-Id", actingUserId.toString())
                }.body<UserResponse>()
        }

    suspend fun deleteUser(
        userIdToDelete: UInt,
        actingUserId: UInt,
    ): Result<Unit> =
        runCatching {
            httpClient.delete(Endpoints.deleteUser(userIdToDelete)) {
                header("X-Mock-User-Id", actingUserId.toString())
            }
            Unit
        }

    suspend fun changePassword(
        userIdToUpdate: UInt,
        actingUserId: UInt,
        request: ChangePasswordRequest,
    ): Result<Unit> =
        runCatching {
            httpClient
                .patch(Endpoints.changePassword(userIdToUpdate)) {
                    contentType(ContentType.Application.Json)
                    setBody(request)
                    header("X-Mock-User-Id", actingUserId.toString())
                }
            Unit
        }
}
