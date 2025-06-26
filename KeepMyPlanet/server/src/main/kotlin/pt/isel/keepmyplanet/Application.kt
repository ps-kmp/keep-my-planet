package pt.isel.keepmyplanet

import io.ktor.server.application.Application
import io.ktor.server.application.install
import io.ktor.server.netty.EngineMain
import io.ktor.server.sse.SSE
import org.koin.ktor.plugin.Koin
import pt.isel.keepmyplanet.di.appModule
import pt.isel.keepmyplanet.plugins.*
import pt.isel.keepmyplanet.plugins.configureAuthentication
import pt.isel.keepmyplanet.plugins.configureCors
import pt.isel.keepmyplanet.plugins.configureLogging
import pt.isel.keepmyplanet.plugins.configureRouting
import pt.isel.keepmyplanet.plugins.configureSerialization
import pt.isel.keepmyplanet.plugins.configureStatusPages

fun main(args: Array<String>): Unit = EngineMain.main(args)

fun Application.module() {
    install(Koin) {
        // Slf4jLogger()
        modules(appModule(this@module))
    }
    configureSerialization()
    configureLogging()
    configureAuthentication()
    configureStatusPages()
    install(SSE)
    configureRouting()
    configureCors()
    configureScheduling()
}
