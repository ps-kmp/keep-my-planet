@file:Suppress("ktlint:standard:no-wildcard-imports")

package pt.isel.keepmyplanet.api

import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.plugins.*
import io.ktor.server.request.*
import io.ktor.server.response.respond
import io.ktor.server.routing.Route
import io.ktor.server.routing.delete
import io.ktor.server.routing.get
import io.ktor.server.routing.post
import io.ktor.server.routing.route
import pt.isel.keepmyplanet.domain.common.Id
import pt.isel.keepmyplanet.dto.message.AddMessageRequest
import pt.isel.keepmyplanet.mapper.message.toResponse
import pt.isel.keepmyplanet.services.MessageService

fun ApplicationCall.getCurrentUserId() = Id(1U)

fun Route.messageWebApi(messageService: MessageService) {
    route("/event/{eventId}/chat") {
        fun ApplicationCall.getEventIdFromPath(): Id {
            val idValue =
                parameters["eventId"]?.toUIntOrNull()
                    ?: throw BadRequestException("Event ID must be a positive integer.")
            try {
                return Id(idValue)
            } catch (e: IllegalArgumentException) {
                throw BadRequestException(e.message ?: "Invalid Event ID format.")
            }
        }

        fun ApplicationCall.getSequenceNumFromPath(): Int {
            val seqNum =
                parameters["seq"]?.toIntOrNull()
                    ?: throw BadRequestException("Message sequence number must be a valid integer.")
            if (seqNum < 0) {
                throw BadRequestException("Message sequence number must be non-negative.")
            }
            return seqNum
        }

        post {
            val eventId = call.getEventIdFromPath()
            val senderId = call.getCurrentUserId()
            val request = call.receive<AddMessageRequest>()

            messageService
                .addMessage(eventId, senderId, request.content)
                .onSuccess { call.respond(HttpStatusCode.Created, it.toResponse()) }
                .onFailure { throw it }
        }

        get {
            val eventId = call.getEventIdFromPath()

            messageService
                .getMessagesForEvent(eventId)
                .onSuccess { msg -> call.respond(HttpStatusCode.OK, msg.map { it.toResponse() }) }
                .onFailure { throw it }
        }

        route("/{seq}") {
            get {
                val eventId = call.getEventIdFromPath()
                val sequenceNum = call.getSequenceNumFromPath()

                messageService
                    .getSingleMessageBySequence(eventId, sequenceNum)
                    .onSuccess { call.respond(HttpStatusCode.OK, it.toResponse()) }
                    .onFailure { throw it }
            }

            delete {
                val eventId = call.getEventIdFromPath()
                val sequenceNum = call.getSequenceNumFromPath()
                val requestingUserId = call.getCurrentUserId()

                messageService
                    .deleteMessage(eventId, sequenceNum, requestingUserId)
                    .onSuccess { call.respond(HttpStatusCode.NoContent) }
                    .onFailure { throw it }
            }
        }
    }
}
