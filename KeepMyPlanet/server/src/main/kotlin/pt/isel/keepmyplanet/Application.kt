@file:Suppress("ktlint:standard:no-wildcard-imports")

package pt.isel.keepmyplanet

import io.ktor.server.application.Application
import io.ktor.server.engine.embeddedServer
import io.ktor.server.netty.Netty
import io.ktor.server.response.respondText
import io.ktor.server.routing.get
import io.ktor.server.routing.routing
import pt.isel.keepmyplanet.api.messageWebApi
import pt.isel.keepmyplanet.repository.mem.InMemoryMessageRepository
import pt.isel.keepmyplanet.services.MessageService

fun main() {
    embeddedServer(Netty, port = SERVER_PORT, host = "0.0.0.0", module = { module() })
        .start(wait = true)
}

fun Application.module() {
    val messageRepository = InMemoryMessageRepository()
    val messageService = MessageService(messageRepository)

    routing {
        get("/") {
            call.respondText("Ktor: ${Greeting().greet()}")
        }
        messageWebApi(messageService)
    }
}
