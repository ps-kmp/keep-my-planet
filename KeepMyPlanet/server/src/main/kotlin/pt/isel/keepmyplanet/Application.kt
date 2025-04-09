@file:Suppress("ktlint:standard:no-wildcard-imports")

package pt.isel.keepmyplanet

import io.ktor.server.application.Application
import io.ktor.server.engine.embeddedServer
import io.ktor.server.netty.Netty
import io.ktor.server.response.respondText
import io.ktor.server.routing.get
import io.ktor.server.routing.routing
import pt.isel.keepmyplanet.api.messageWebApi
import pt.isel.keepmyplanet.api.zoneWebApi
import pt.isel.keepmyplanet.plugins.configureStatusPages
import pt.isel.keepmyplanet.repository.mem.InMemoryMessageRepository
import pt.isel.keepmyplanet.repository.mem.InMemoryZoneRepository
import pt.isel.keepmyplanet.services.MessageService
import pt.isel.keepmyplanet.services.ZoneService

fun main() {
    embeddedServer(Netty, port = SERVER_PORT, host = "0.0.0.0", module = { module() })
        .start(wait = true)
}

fun Application.module() {
    val zoneRepository = InMemoryZoneRepository()
    val zoneService = ZoneService(zoneRepository)
    val messageRepository = InMemoryMessageRepository()
    val messageService = MessageService(messageRepository)

    configureStatusPages()
    routing {
        get("/") {
            call.respondText("Ktor: ${Greeting().greet()}")
        }
        zoneWebApi(zoneService)
        messageWebApi(messageService)
    }
}
