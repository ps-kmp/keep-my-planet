package pt.isel.keepmyplanet.data.api

import io.ktor.client.HttpClient
import io.ktor.client.engine.HttpClientEngineFactory
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.defaultRequest
import io.ktor.client.plugins.sse.SSE
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.json.Json
import pt.isel.keepmyplanet.BASE_URL

expect fun httpClientEngine(): HttpClientEngineFactory<*>

fun createHttpClient(token: String?): HttpClient =
    HttpClient(httpClientEngine()) {
        defaultRequest {
            url(BASE_URL)
            // token?.let { header(HttpHeaders.Authorization, "Bearer $it") }
        }
        install(ContentNegotiation) {
            json(Json { ignoreUnknownKeys = true })
        }
        install(SSE)
        // token?.let { install(Auth) { bearer { loadTokens { BearerTokens(accessToken = it, refreshToken = "") } } } }
    }
