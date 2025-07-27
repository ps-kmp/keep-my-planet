package pt.isel.keepmyplanet.plugins

import io.ktor.http.HttpHeaders
import io.ktor.http.HttpMethod
import io.ktor.server.application.Application
import io.ktor.server.application.install
import io.ktor.server.plugins.cors.routing.CORS

fun Application.configureCors() {
    // val frontendUrl = environment.config.propertyOrNull("cors.frontendUrl")?.getString()

    install(CORS) {
        allowMethod(HttpMethod.Options)
        allowMethod(HttpMethod.Put)
        allowMethod(HttpMethod.Delete)
        allowMethod(HttpMethod.Patch)
        allowMethod(HttpMethod.Get)
        allowMethod(HttpMethod.Post)
        allowHeader(HttpHeaders.Authorization)
        allowHeader(HttpHeaders.ContentType)
        allowHeader(HttpHeaders.Accept)
        allowHeader(HttpHeaders.CacheControl)
        allowHeader("X-Requested-With")
        allowCredentials = true
        allowNonSimpleContentTypes = true
        anyHost()
//        frontendUrl?.let {
//            val uri = URI(it)
//            allowHost(uri.host, schemes = listOf(uri.scheme))
//        }
    }
}
