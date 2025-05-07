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
    // Data Access Layer
    val userRepository = InMemoryUserRepository()
    val zoneRepository = InMemoryZoneRepository()
    val eventRepository = InMemoryEventRepository(zoneRepository)
    val eventStateChangeRepository = InMemoryEventStateChangeRepository()
    val messageRepository = InMemoryMessageRepository()

    // Components
    val passwordHasher = PasswordHasher()
    val chatSseService = ChatSseService()

    // Service Layer
    val userService = UserService(userRepository, eventRepository, passwordHasher)
    val zoneService = ZoneService(zoneRepository, userRepository, eventRepository)
    val eventService =
        EventService(eventRepository, zoneRepository, userRepository, messageRepository)
    val eventStateChangeService =
        EventStateChangeService(eventRepository, eventStateChangeRepository)
    val messageService =
        MessageService(messageRepository, eventRepository, userRepository, chatSseService)

    // Presentation Layer
    routing {
        get("/") {
            call.respondText("Ktor: ${Greeting().greet()}")
        }

        // API Routes
        userWebApi(userService)
        zoneWebApi(zoneService)
        eventWebApi(eventService, eventStateChangeService)
        messageWebApi(messageService, chatSseService)
    }
}
