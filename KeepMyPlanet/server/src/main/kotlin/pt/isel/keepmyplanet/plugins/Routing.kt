package pt.isel.keepmyplanet.plugins

import io.ktor.server.application.Application
import io.ktor.server.response.respondText
import io.ktor.server.routing.get
import io.ktor.server.routing.routing
import pt.isel.keepmyplanet.Greeting
import pt.isel.keepmyplanet.api.eventWebApi
import pt.isel.keepmyplanet.api.messageWebApi
import pt.isel.keepmyplanet.api.userWebApi
import pt.isel.keepmyplanet.api.zoneWebApi
import pt.isel.keepmyplanet.repository.mem.InMemoryEventRepository
import pt.isel.keepmyplanet.repository.mem.InMemoryEventStateChangeRepository
import pt.isel.keepmyplanet.repository.mem.InMemoryMessageRepository
import pt.isel.keepmyplanet.repository.mem.InMemoryUserRepository
import pt.isel.keepmyplanet.repository.mem.InMemoryZoneRepository
import pt.isel.keepmyplanet.service.ChatSseService
import pt.isel.keepmyplanet.service.EventService
import pt.isel.keepmyplanet.service.EventStateChangeService
import pt.isel.keepmyplanet.service.MessageService
import pt.isel.keepmyplanet.service.UserService
import pt.isel.keepmyplanet.service.ZoneService
import pt.isel.keepmyplanet.util.PasswordHasher

fun Application.configureRouting() {
    val zoneRepository = InMemoryZoneRepository()
    val userRepository = InMemoryUserRepository()
    val eventRepository = InMemoryEventRepository(zoneRepository)
    val messageRepository = InMemoryMessageRepository()
    val eventStateChangeRepository = InMemoryEventStateChangeRepository()

    val zoneService = ZoneService(zoneRepository, userRepository, eventRepository)
    val chatSseService = ChatSseService()
    val messageService = MessageService(messageRepository, eventRepository, userRepository, chatSseService)
    val eventService = EventService(eventRepository, zoneRepository)
    val eventStateChangeService = EventStateChangeService(eventRepository, eventStateChangeRepository)

    val passwordHasher = PasswordHasher()
    val userService = UserService(userRepository, eventRepository, passwordHasher)

    routing {
        get("/") {
            call.respondText("Ktor: ${Greeting().greet()}")
        }

        zoneWebApi(zoneService)
        messageWebApi(messageService, chatSseService)
        userWebApi(userService)
        eventWebApi(eventService, eventStateChangeService)
    }
}
