package pt.isel.keepmyplanet.plugins

import io.ktor.server.application.Application
import io.ktor.server.http.content.staticResources
import io.ktor.server.response.respondText
import io.ktor.server.routing.get
import io.ktor.server.routing.route
import io.ktor.server.routing.routing
import org.koin.ktor.ext.inject
import pt.isel.keepmyplanet.api.authWebApi
import pt.isel.keepmyplanet.api.deviceWebApi
import pt.isel.keepmyplanet.api.eventWebApi
import pt.isel.keepmyplanet.api.ipGeocodingWebApi
import pt.isel.keepmyplanet.api.messageWebApi
import pt.isel.keepmyplanet.api.photoWebApi
import pt.isel.keepmyplanet.api.userWebApi
import pt.isel.keepmyplanet.api.zoneWebApi
import pt.isel.keepmyplanet.service.AuthService
import pt.isel.keepmyplanet.service.ChatSseService
import pt.isel.keepmyplanet.service.EventService
import pt.isel.keepmyplanet.service.EventStateChangeService
import pt.isel.keepmyplanet.service.IpGeocodingService
import pt.isel.keepmyplanet.service.MessageService
import pt.isel.keepmyplanet.service.NotificationService
import pt.isel.keepmyplanet.service.PhotoService
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
    val photoService by inject<PhotoService>()
    val notificationService by inject<NotificationService>()
    val ipGeocodingService by inject<IpGeocodingService>()

    // Presentation Layer
    routing {
        get("/healthz") {
            call.respondText("OK")
        }

        route("/") {
            authWebApi(authService)
            userWebApi(userService)
            zoneWebApi(zoneService)
            eventWebApi(eventService, eventStateChangeService)
            messageWebApi(messageService, chatSseService)
            photoWebApi(photoService)
            deviceWebApi(notificationService)
            ipGeocodingWebApi(ipGeocodingService)
        }

        staticResources("/", "static") {
            default("index.html")
        }
    }
}
