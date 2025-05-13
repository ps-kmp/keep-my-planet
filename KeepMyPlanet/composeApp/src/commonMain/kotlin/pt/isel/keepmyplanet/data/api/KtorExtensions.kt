package pt.isel.keepmyplanet.data.api

import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.HttpRequestBuilder
import io.ktor.client.request.header
import io.ktor.client.request.request

suspend inline fun <reified T> HttpClient.executeRequest(block: HttpRequestBuilder.() -> Unit): Result<T> =
    runCatching {
        request { block() }.body<T>()
    }

suspend fun HttpClient.executeRequestUnit(block: HttpRequestBuilder.() -> Unit): Result<Unit> =
    runCatching {
        request { block() }
        Unit
    }

fun HttpRequestBuilder.mockUser(userId: UInt) {
    header("X-Mock-User-Id", userId.toString())
}
