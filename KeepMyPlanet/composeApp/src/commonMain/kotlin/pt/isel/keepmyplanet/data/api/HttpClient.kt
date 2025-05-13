package pt.isel.keepmyplanet.data.api

import io.ktor.client.HttpClient
import io.ktor.client.engine.HttpClientEngineFactory
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.defaultRequest
import io.ktor.client.plugins.logging.DEFAULT
import io.ktor.client.plugins.logging.LogLevel
import io.ktor.client.plugins.logging.Logger
import io.ktor.client.plugins.logging.Logging
import io.ktor.client.plugins.sse.SSE
import io.ktor.client.request.header
import io.ktor.http.ContentType
import io.ktor.http.HttpHeaders
import io.ktor.http.contentType
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.json.Json
import pt.isel.keepmyplanet.BASE_URL

expect fun httpClientEngine(): HttpClientEngineFactory<*>

fun createHttpClient(tokenProvider: () -> String?): HttpClient =
    HttpClient(httpClientEngine()) {
        defaultRequest {
            url(BASE_URL)
            contentType(ContentType.Application.Json)
            tokenProvider()?.let { header(HttpHeaders.Authorization, "Bearer $it") }
        }
        install(ContentNegotiation) {
            json(
                Json {
                    ignoreUnknownKeys = true
                    prettyPrint = true
                    isLenient = true
                    encodeDefaults = true
                },
            )
        }
        install(SSE)
        install(Logging) {
            logger = Logger.DEFAULT
            level = LogLevel.ALL
        }
        // install(Auth) { bearer { loadTokens { val currentToken = tokenProvider(); if (currentToken != null) BearerTokens(accessToken = currentToken, refreshToken = "") else null } } }
    }
