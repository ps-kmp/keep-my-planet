@file:Suppress("ktlint:standard:no-wildcard-imports")

package pt.isel.keepmyplanet.api

import io.ktor.http.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import pt.isel.keepmyplanet.domain.common.Id
import pt.isel.keepmyplanet.domain.message.MessageContent
import pt.isel.keepmyplanet.services.MessageService

fun Route.messageWebApi(messageService: MessageService) {
    route("/chat/{eventId}") {
        // Get event messages
        get {
            val eventId = call.parameters["eventId"]?.toUIntOrNull()
            if (eventId != null) {
                val messages = messageService.getMessages(Id(eventId))
                call.respond(messages)
            } else {
                call.respondText("Invalid event ID", status = HttpStatusCode.BadRequest)
            }
        }

        // Add message
        post {
            val eventId = call.parameters["eventId"]?.toUIntOrNull()
            val senderId = call.receive<String>()
            val content = call.receive<String>()

            if (eventId != null) {
                val newMessage = messageService.addMessage(Id(eventId), senderId, MessageContent(content))
                call.respond(newMessage)
            } else {
                call.respondText("Invalid event ID", status = HttpStatusCode.BadRequest)
            }
        }
    }
}
