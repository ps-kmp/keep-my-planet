package pt.isel.keepmyplanet.api

import io.ktor.http.HttpStatusCode
import io.ktor.server.auth.authenticate
import io.ktor.server.request.receive
import io.ktor.server.response.respond
import io.ktor.server.routing.Route
import io.ktor.server.routing.post
import io.ktor.server.routing.route
import pt.isel.keepmyplanet.dto.notification.RegisterDeviceRequest
import pt.isel.keepmyplanet.service.NotificationService
import pt.isel.keepmyplanet.utils.getCurrentUserId

fun Route.deviceWebApi(notificationService: NotificationService) {
    route("/devices") {
        authenticate("auth-jwt") {
            post("/register") {
                val userId = call.getCurrentUserId()
                val request = call.receive<RegisterDeviceRequest>()

                notificationService.registerDevice(userId, request.token, request.platform)
                call.respond(HttpStatusCode.OK)
            }
        }
    }
}
