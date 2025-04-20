package pt.isel.keepmyplanet.api

import io.ktor.http.HttpStatusCode
import io.ktor.server.application.ApplicationCall
import io.ktor.server.request.receive
import io.ktor.server.response.respond
import io.ktor.server.routing.Route
import io.ktor.server.routing.delete
import io.ktor.server.routing.get
import io.ktor.server.routing.post
import io.ktor.server.routing.route
import pt.isel.keepmyplanet.domain.common.Id
import pt.isel.keepmyplanet.dto.message.AddMessageRequest
import pt.isel.keepmyplanet.errors.ValidationException
import pt.isel.keepmyplanet.mapper.message.toResponse
import pt.isel.keepmyplanet.service.MessageService

fun getCurrentUserId() = Id(1U)

fun Route.messageWebApi(messageService: MessageService) {
    route("/event/{eventId}/chat") {
        fun ApplicationCall.getEventIdFromPath(): Id {
            val idValue =
                parameters["eventId"]?.toUIntOrNull()
                    ?: throw ValidationException("Event ID must be a positive integer.")
            return Id(idValue)
        }

        fun ApplicationCall.getSequenceNumFromPath(): Int {
            val seqNum =
                parameters["seq"]?.toIntOrNull()
                    ?: throw ValidationException("Message sequence number must be a valid integer.")
            if (seqNum < 0) {
                throw ValidationException("Message sequence number must be positive.")
            }
            return seqNum
        }

        // Add message to chat
        post {
            val eventId = call.getEventIdFromPath()
            val senderId = getCurrentUserId()
            val request = call.receive<AddMessageRequest>()

            messageService
                .addMessage(eventId, senderId, request.content)
                .onSuccess { call.respond(HttpStatusCode.Created, it.toResponse()) }
                .onFailure { throw it }
        }

        // Get all messages from chat
        get {
            val eventId = call.getEventIdFromPath()

            messageService
                .getAllMessagesFromEvent(eventId)
                .onSuccess { msg ->
                    call.respond(HttpStatusCode.OK, msg.map { it.toResponse() }.toList())
                }.onFailure { throw it }
        }

        route("/{seq}") {
            // Get a single message from chat
            get {
                val eventId = call.getEventIdFromPath()
                val sequenceNum = call.getSequenceNumFromPath()

                messageService
                    .getSingleMessageBySequence(eventId, sequenceNum)
                    .onSuccess { call.respond(HttpStatusCode.OK, it.toResponse()) }
                    .onFailure { throw it }
            }

            // Delete a single message from chat
            delete {
                val eventId = call.getEventIdFromPath()
                val sequenceNum = call.getSequenceNumFromPath()
                val requestingUserId = getCurrentUserId()

                messageService
                    .deleteMessage(eventId, sequenceNum, requestingUserId)
                    .onSuccess { call.respond(HttpStatusCode.NoContent) }
                    .onFailure { throw it }
            }
        }
    }
}
