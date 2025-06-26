package pt.isel.keepmyplanet.plugins

import io.ktor.server.application.Application
import io.ktor.server.response.respondText
import io.ktor.server.routing.get
import io.ktor.server.routing.routing
import org.koin.ktor.ext.inject
import pt.isel.keepmyplanet.Greeting
import pt.isel.keepmyplanet.api.authWebApi
import pt.isel.keepmyplanet.api.eventWebApi
import pt.isel.keepmyplanet.api.messageWebApi
import pt.isel.keepmyplanet.api.userWebApi
import pt.isel.keepmyplanet.api.zoneWebApi
import pt.isel.keepmyplanet.service.AuthService
import pt.isel.keepmyplanet.service.ChatSseService
import pt.isel.keepmyplanet.service.EventService
import pt.isel.keepmyplanet.service.EventStateChangeService
import pt.isel.keepmyplanet.service.MessageService
import pt.isel.keepmyplanet.service.UserService
import pt.isel.keepmyplanet.service.ZoneService

fun Application.configureRouting() {
    val authService by inject<AuthService>()
    val userService by inject<UserService>()
    val zoneService by inject<ZoneService>()
    val eventService by inject<EventService>()
    val eventStateChangeService by inject<EventStateChangeService>()
    val messageService by inject<MessageService>()
    val chatSseService by inject<ChatSseService>()

    // Presentation Layer
    routing {
        get("/") { call.respondText("Ktor: ${Greeting().greet()}") }
        authWebApi(authService)
        userWebApi(userService)
        zoneWebApi(zoneService)
        eventWebApi(eventService, eventStateChangeService)
        messageWebApi(messageService, chatSseService)
    }
}
