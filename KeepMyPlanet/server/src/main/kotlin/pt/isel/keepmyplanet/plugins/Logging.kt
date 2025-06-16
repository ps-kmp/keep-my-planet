package pt.isel.keepmyplanet.plugins

import io.ktor.http.HttpMethod
import io.ktor.server.application.Application
import io.ktor.server.application.install
import io.ktor.server.plugins.calllogging.CallLogging
import io.ktor.server.request.httpMethod
import io.ktor.server.request.path
import org.slf4j.event.Level

fun Application.configureLogging() {
    install(CallLogging) {
        level = Level.INFO
        filter { call ->
            val path = call.request.path()
            val method = call.request.httpMethod
            when {
                path == "/auth/login" && method == HttpMethod.Post -> false
                path == "/users" && method == HttpMethod.Post -> false
                path.contains("/password") &&
                    (method == HttpMethod.Patch || method == HttpMethod.Post) -> false

                else -> true
            }
        }
        format { call ->
            val status = call.response.status()
            val httpMethod = call.request.httpMethod.value
            val userAgent = call.request.headers["User-Agent"]
            "Status: $status, HTTP method: $httpMethod, User agent: $userAgent"
        }
    }
}
