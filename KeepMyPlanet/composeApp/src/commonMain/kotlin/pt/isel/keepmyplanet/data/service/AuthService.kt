package pt.isel.keepmyplanet.data.service

import io.ktor.client.HttpClient
import pt.isel.keepmyplanet.data.model.UserInfo
import pt.isel.keepmyplanet.data.model.UserSession

class AuthService(
    private val httpClient: HttpClient,
) {
    private val validUsers =
        mapOf(
            "rafael" to "pass",
            "diogo" to "pass",
            "user" to "pass",
        )

    private val userIds =
        mapOf(
            "rafael" to 1U,
            "diogo" to 2U,
            "user" to 3U,
        )

    suspend fun login(
        username: String,
        password: String,
    ): Result<UserSession> =
        if (validUsers[username] == password) {
            val userId = userIds[username] ?: 0U
            if (userId != 0U) {
                Result.success(UserSession(UserInfo(userId, username, "f", null), token = "token"))
            } else {
                Result.failure(Exception("User ID not found for username."))
            }
        } else {
            Result.failure(Exception("Invalid username or password."))
        }
}
