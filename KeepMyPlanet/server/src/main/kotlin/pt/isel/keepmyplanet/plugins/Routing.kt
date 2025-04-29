package pt.isel.keepmyplanet.plugins

import io.ktor.server.application.Application
import io.ktor.server.response.respondText
import io.ktor.server.routing.get
import io.ktor.server.routing.routing
import pt.isel.keepmyplanet.Greeting
import pt.isel.keepmyplanet.api.messageWebApi
import pt.isel.keepmyplanet.api.zoneWebApi
import pt.isel.keepmyplanet.repository.mem.InMemoryEventRepository
import pt.isel.keepmyplanet.repository.mem.InMemoryMessageRepository
import pt.isel.keepmyplanet.repository.mem.InMemoryUserRepository
import pt.isel.keepmyplanet.repository.mem.InMemoryZoneRepository
import pt.isel.keepmyplanet.service.ChatSseService
import pt.isel.keepmyplanet.service.MessageService
import pt.isel.keepmyplanet.service.ZoneService

fun Application.configureRouting() {
    val zoneRepository = InMemoryZoneRepository()
    val userRepository = InMemoryUserRepository()
    val eventRepository = InMemoryEventRepository(zoneRepository)
    val messageRepository = InMemoryMessageRepository()
    val zoneService = ZoneService(zoneRepository, userRepository, eventRepository)
    val chatSseService = ChatSseService()
    val messageService = MessageService(messageRepository, eventRepository, userRepository, chatSseService)

    routing {
        get("/") {
            call.respondText("Ktor: ${Greeting().greet()}")
        }

        zoneWebApi(zoneService)
        messageWebApi(messageService, chatSseService)
    }
}
