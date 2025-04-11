@file:Suppress("ktlint:standard:no-wildcard-imports")

package pt.isel.keepmyplanet.api

import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.plugins.*
import io.ktor.server.request.*
import io.ktor.server.response.respond
import io.ktor.server.routing.Route
import io.ktor.server.routing.get
import io.ktor.server.routing.post
import io.ktor.server.routing.route
import pt.isel.keepmyplanet.domain.common.Id
import pt.isel.keepmyplanet.dto.message.AddMessageRequest
import pt.isel.keepmyplanet.mapper.message.toResponse
import pt.isel.keepmyplanet.services.MessageService

fun Route.messageWebApi(messageService: MessageService) {
    route("/event/{eventId}/chat") {
        fun ApplicationCall.getEventIdFromPath(): Id {
            val idValue =
                parameters["eventId"]?.toUIntOrNull()
                    ?: throw NumberFormatException("Event ID must be a positive integer.")
            try {
                return Id(idValue)
            } catch (e: IllegalArgumentException) {
                throw BadRequestException(e.message ?: "Invalid Event ID format.")
            }
        }

        // Get all event messages
        get {
            val eventId = call.getEventIdFromPath()
            messageService
                .getMessages(eventId)
                .onSuccess { messages ->
                    call.respond(HttpStatusCode.OK, messages.map { it.toResponse() })
                }.onFailure { throw it }
        }

        // Get specific message in chat
        get("/{msgNum}") {
            val eventId = call.getEventIdFromPath()
            val messageNum =
                call.parameters["msgNum"]?.toIntOrNull()
                    ?: throw NumberFormatException("Message number must be an integer.")

            if (messageNum < 0) {
                throw BadRequestException("Message number must be non-negative.")
            }

            messageService
                .getSingleMessage(eventId, messageNum)
                .onSuccess { message ->
                    call.respond(HttpStatusCode.OK, message.toResponse())
                }.onFailure { throw it }
        }

        // Add message
        post("/add-message") {
            val eventId = call.getEventIdFromPath()
            val request = call.receive<AddMessageRequest>()
            val senderId = Id(request.senderId)
            val content = request.content

            messageService
                .addMessage(eventId, senderId, content)
                .onSuccess { message ->
                    call.respond(HttpStatusCode.Created, message.toResponse())
                }.onFailure { throw it }
        }
    }
}
