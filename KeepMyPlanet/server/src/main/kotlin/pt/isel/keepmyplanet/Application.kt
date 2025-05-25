package pt.isel.keepmyplanet

import io.ktor.server.application.Application
import io.ktor.server.application.install
import io.ktor.server.netty.EngineMain
import io.ktor.server.sse.SSE
import pt.isel.keepmyplanet.plugins.configureAuthentication
import pt.isel.keepmyplanet.plugins.configureDatabase
import pt.isel.keepmyplanet.plugins.configureLogging
import pt.isel.keepmyplanet.plugins.configureRouting
import pt.isel.keepmyplanet.plugins.configureSerialization
import pt.isel.keepmyplanet.plugins.configureStatusPages

fun main(args: Array<String>): Unit = EngineMain.main(args)

fun Application.module() {
    configureDatabase()
    configureSerialization()
    configureLogging()
    configureAuthentication()
    configureStatusPages()
    install(SSE)
    configureRouting()
}
