package pt.isel.keepmyplanet.plugins

import io.ktor.http.HttpHeaders
import io.ktor.http.HttpMethod
import io.ktor.server.application.Application
import io.ktor.server.application.install
import io.ktor.server.plugins.cors.routing.CORS

fun Application.configureCors() {
    install(CORS) {
        allowMethod(HttpMethod.Options)
        allowMethod(HttpMethod.Put)
        allowMethod(HttpMethod.Delete)
        allowMethod(HttpMethod.Patch)
        allowMethod(HttpMethod.Get)
        allowMethod(HttpMethod.Post)
        allowHeader(HttpHeaders.Authorization)
        allowHeader(HttpHeaders.ContentType)
        allowCredentials = true
        allowNonSimpleContentTypes = true
        try {
            val frontendUrl =
                this@configureCors.environment.config.property("cors.frontendUrl").getString()
            val (scheme, host) = frontendUrl.split("://")
            allowHost(host, schemes = listOf(scheme))
        } catch (_: Exception) {
        }
        allowHost("localhost", schemes = listOf("http", "https"))
        allowHost("127.0.0.1", schemes = listOf("http", "https"))
    }
}
