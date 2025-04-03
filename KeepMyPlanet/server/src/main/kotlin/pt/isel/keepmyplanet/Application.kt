@file:Suppress("ktlint:standard:no-wildcard-imports")

package pt.isel.keepmyplanet

import io.ktor.server.application.*
import io.ktor.server.engine.*
import io.ktor.server.netty.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import pt.isel.keepmyplanet.api.messageWebApi
import pt.isel.keepmyplanet.repository.mem.InMemMessageRepository
import pt.isel.keepmyplanet.services.MessageService

fun main() {
    embeddedServer(Netty, port = SERVER_PORT, host = "0.0.0.0", module = { module() })
        .start(wait = true)
}

fun Application.module() {
    val messageRepository = InMemMessageRepository()
    val messageService = MessageService(messageRepository)

    routing {
        get("/") {
            call.respondText("Ktor: ${Greeting().greet()}")
        }
        messageWebApi(messageService)
    }
}
