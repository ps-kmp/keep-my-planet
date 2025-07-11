package pt.isel.keepmyplanet.data.http

import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.plugins.ClientRequestException
import io.ktor.client.request.HttpRequestBuilder
import io.ktor.client.request.request
import io.ktor.client.statement.HttpResponse
import io.ktor.http.isSuccess
import kotlin.coroutines.cancellation.CancellationException
import kotlinx.serialization.json.Json
import pt.isel.keepmyplanet.dto.error.ErrorResponse
import pt.isel.keepmyplanet.exception.AppException
import pt.isel.keepmyplanet.exception.AuthenticationException
import pt.isel.keepmyplanet.exception.AuthorizationException
import pt.isel.keepmyplanet.exception.ConflictException
import pt.isel.keepmyplanet.exception.InternalServerException
import pt.isel.keepmyplanet.exception.NotFoundException
import pt.isel.keepmyplanet.exception.ValidationException

suspend fun handleRequestException(e: ClientRequestException): Nothing {
    val errorResponse =
        try {
            Json.decodeFromString<ErrorResponse>(e.response.body())
        } catch (_: Exception) {
            null
        }
    throw errorResponse?.let { ApiException(it) } ?: e
}

suspend inline fun <reified T> HttpClient.executeRequest(
    crossinline block: HttpRequestBuilder.() -> Unit,
): Result<T> =
    try {
        val response: HttpResponse = request { block() }

        if (response.status.isSuccess()) {
            Result.success(response.body<T>())
        } else {
            val errorResponse = response.body<ErrorResponse>()
            Result.failure(mapErrorResponseToException(errorResponse, response.status.value))
        }
    } catch (e: CancellationException) {
        throw e
    } catch (e: Exception) {
        println(
            "Request failed with a non-server exception: ${e::class.simpleName} -> ${e.message}",
        )
        Result.failure(
            InternalServerException(
                "Could not connect to the server or process its response.",
                e,
            ),
        )
    }

suspend inline fun HttpClient.executeRequestUnit(
    crossinline block: HttpRequestBuilder.() -> Unit,
): Result<Unit> = executeRequest<Unit>(block)

fun mapErrorResponseToException(
    error: ErrorResponse,
    status: Int,
): AppException {
    val message = error.message
    return when (status) {
        400 -> ValidationException(message)
        401 -> AuthenticationException(message)
        403 -> AuthorizationException(message)
        404 -> NotFoundException(message)
        409 -> ConflictException(message)
        500 -> InternalServerException(message)
        else -> InternalServerException("An unexpected error occurred (Status $status): $message")
    }
}
