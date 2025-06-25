package pt.isel.keepmyplanet.plugins

import io.ktor.server.application.Application
import io.ktor.server.response.respondText
import io.ktor.server.routing.get
import io.ktor.server.routing.routing
import pt.isel.keepmyplanet.Greeting
import pt.isel.keepmyplanet.api.authWebApi
import pt.isel.keepmyplanet.api.eventWebApi
import pt.isel.keepmyplanet.api.messageWebApi
import pt.isel.keepmyplanet.api.userWebApi
import pt.isel.keepmyplanet.api.zoneWebApi
import pt.isel.keepmyplanet.repository.database.DatabaseEventRepository
import pt.isel.keepmyplanet.repository.database.DatabaseEventStateChangeRepository
import pt.isel.keepmyplanet.repository.database.DatabaseMessageRepository
import pt.isel.keepmyplanet.repository.database.DatabaseUserRepository
import pt.isel.keepmyplanet.repository.database.DatabaseZoneRepository
import pt.isel.keepmyplanet.security.Pbkdf2PasswordHasher
import pt.isel.keepmyplanet.service.AuthService
import pt.isel.keepmyplanet.service.ChatSseService
import pt.isel.keepmyplanet.service.EventService
import pt.isel.keepmyplanet.service.EventStateChangeService
import pt.isel.keepmyplanet.service.JwtService
import pt.isel.keepmyplanet.service.MessageService
import pt.isel.keepmyplanet.service.UserService
import pt.isel.keepmyplanet.service.ZoneService

fun Application.configureRouting() {
    // Data Access Layer
    val userRepository = DatabaseUserRepository(database.userQueries)
    val zoneRepository = DatabaseZoneRepository(database.zoneQueries)
    val eventRepository = DatabaseEventRepository(database.eventQueries)
    val eventStateChangeRepository =
        DatabaseEventStateChangeRepository(database.eventStateChangeQueries)
    val messageRepository = DatabaseMessageRepository(database.messageQueries)

    // Components
    val jwtService = JwtService(environment.config)
    val passwordHasher = Pbkdf2PasswordHasher()
    val chatSseService = ChatSseService()

    // Service Layer
    val authService = AuthService(userRepository, passwordHasher, jwtService)
    val userService = UserService(userRepository, eventRepository, passwordHasher)
    val zoneService = ZoneService(zoneRepository, userRepository, eventRepository)
    val eventService =
        EventService(eventRepository, zoneRepository, userRepository, messageRepository)
    val eventStateChangeService =
        EventStateChangeService(
            eventRepository,
            zoneRepository,
            eventStateChangeRepository,
            userRepository,
        )
    val messageService =
        MessageService(messageRepository, eventRepository, userRepository, chatSseService)

    // Presentation Layer
    routing {
        get("/") {
            call.respondText("Ktor: ${Greeting().greet()}")
        }

        // API Routes
        authWebApi(authService)
        userWebApi(userService)
        zoneWebApi(zoneService)
        eventWebApi(eventService, eventStateChangeService)
        messageWebApi(messageService, chatSseService)
    }
}
