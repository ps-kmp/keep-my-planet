package pt.isel.keepmyplanet.data.service

import io.ktor.client.HttpClient
import io.ktor.client.request.setBody
import io.ktor.client.request.url
import io.ktor.http.HttpMethod
import pt.isel.keepmyplanet.data.api.executeRequest
import pt.isel.keepmyplanet.dto.auth.LoginRequest
import pt.isel.keepmyplanet.dto.auth.LoginResponse

class AuthService(
    private val httpClient: HttpClient,
) {
    private object Endpoints {
        const val AUTH_BASE = "auth"

        fun login() = "$AUTH_BASE/login"
    }

    suspend fun login(request: LoginRequest): Result<LoginResponse> =
        httpClient.executeRequest {
            method = HttpMethod.Post
            url(Endpoints.login())
            setBody(request)
        }
}
