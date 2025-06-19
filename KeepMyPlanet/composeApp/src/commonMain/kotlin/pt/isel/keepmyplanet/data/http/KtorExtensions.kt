package pt.isel.keepmyplanet.data.http

import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.plugins.ClientRequestException
import io.ktor.client.request.HttpRequestBuilder
import io.ktor.client.request.request
import kotlinx.serialization.json.Json
import pt.isel.keepmyplanet.dto.common.ErrorResponse

suspend inline fun <reified T> HttpClient.executeRequest(
    block: HttpRequestBuilder.() -> Unit,
): Result<T> =
    runCatching {
        try {
            request { block() }.body<T>()
        } catch (e: ClientRequestException) {
            val errorResponse =
                try {
                    Json.decodeFromString<ErrorResponse>(e.response.body())
                } catch (_: Exception) {
                    null
                }
            throw errorResponse?.let { ApiException(it) } ?: e
        }
    }

suspend fun HttpClient.executeRequestUnit(block: HttpRequestBuilder.() -> Unit): Result<Unit> =
    runCatching {
        try {
            request { block() }
            Unit
        } catch (e: ClientRequestException) {
            val errorResponse =
                try {
                    Json.decodeFromString<ErrorResponse>(e.response.body())
                } catch (_: Exception) {
                    null
                }
            throw errorResponse?.let { ApiException(it) } ?: e
        }
    }
