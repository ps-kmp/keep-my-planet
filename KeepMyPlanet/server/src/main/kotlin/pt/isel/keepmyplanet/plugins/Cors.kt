package pt.isel.keepmyplanet.plugins

import io.ktor.http.HttpHeaders
import io.ktor.http.HttpMethod
import io.ktor.server.application.Application
import io.ktor.server.application.install
import io.ktor.server.plugins.cors.routing.CORS

fun Application.configureCors() {
    install(CORS) {
        val frontendUrl =
            this@configureCors
                .environment.config
                .propertyOrNull("cors.frontendUrl")
                ?.getString()
        if (frontendUrl != null) {
            allowHost(frontendUrl.removePrefix("https://"), schemes = listOf("https"))
        }
        allowHost("localhost:8080", schemes = listOf("http"))

        allowHeader(HttpHeaders.ContentType)
        allowHeader(HttpHeaders.Authorization)
        allowCredentials = true
        allowMethod(HttpMethod.Get)
        allowMethod(HttpMethod.Post)
        allowMethod(HttpMethod.Put)
        allowMethod(HttpMethod.Delete)
        allowMethod(HttpMethod.Patch)
        allowMethod(HttpMethod.Options)
        anyHost()
    }
}
