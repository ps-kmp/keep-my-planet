package pt.isel.keepmyplanet

import io.ktor.server.application.Application
import io.ktor.server.application.install
import io.ktor.server.engine.embeddedServer
import io.ktor.server.netty.Netty
import io.ktor.server.sse.SSE
import pt.isel.keepmyplanet.plugins.configureLogging
import pt.isel.keepmyplanet.plugins.configureRouting
import pt.isel.keepmyplanet.plugins.configureSerialization
import pt.isel.keepmyplanet.plugins.configureStatusPages

fun main() {
    embeddedServer(Netty, port = SERVER_PORT, host = "0.0.0.0", module = { module() })
        .start(wait = true)
}

fun Application.module() {
    configureSerialization()
    configureLogging()
    // configureAuth()
    configureStatusPages()
    install(SSE)
    configureRouting()
}
