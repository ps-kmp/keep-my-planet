package pt.isel.keepmyplanet.data.api

import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.client.statement.HttpResponse
import io.ktor.http.ContentType
import io.ktor.http.contentType
import io.ktor.http.isSuccess
import pt.isel.keepmyplanet.dto.auth.LoginRequest
import pt.isel.keepmyplanet.dto.auth.LoginResponse
import pt.isel.keepmyplanet.dto.error.ErrorResponse
import pt.isel.keepmyplanet.exception.AuthenticationException
import pt.isel.keepmyplanet.exception.AuthorizationException
import pt.isel.keepmyplanet.exception.ConflictException
import pt.isel.keepmyplanet.exception.InternalServerException
import pt.isel.keepmyplanet.exception.NotFoundException
import pt.isel.keepmyplanet.exception.ValidationException

class AuthApi(
    private val httpClient: HttpClient,
) {
    private object Endpoints {
        const val AUTH_BASE = "auth"

        fun login() = "$AUTH_BASE/login"
    }

    suspend fun login(request: LoginRequest): Result<LoginResponse> {
        return try {
            val response: HttpResponse =
                httpClient.post(Endpoints.login()) {
                    contentType(ContentType.Application.Json)
                    setBody(request)
                }

            if (response.status.isSuccess()) {
                Result.success(response.body<LoginResponse>())
            } else {
                val errorResponse = response.body<ErrorResponse>()
                val appException =
                    when (response.status.value) {
                        400 -> ValidationException(errorResponse.message)
                        401 -> AuthenticationException(errorResponse.message)
                        403 -> AuthorizationException(errorResponse.message)
                        404 -> NotFoundException(errorResponse.message)
                        409 -> ConflictException(errorResponse.message)
                        else -> InternalServerException(errorResponse.message)
                    }
                Result.failure(appException)
            }
        } catch (e: Exception) {
            Result.failure(
                InternalServerException("Could not connect or an unexpected error occurred.", e),
            )
        }
    }
}
