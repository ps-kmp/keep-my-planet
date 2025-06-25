package pt.isel.keepmyplanet.api

import io.ktor.http.HttpStatusCode
import io.ktor.server.application.ApplicationCall
import io.ktor.server.auth.authenticate
import io.ktor.server.request.receive
import io.ktor.server.response.respond
import io.ktor.server.routing.Route
import io.ktor.server.routing.delete
import io.ktor.server.routing.get
import io.ktor.server.routing.post
import io.ktor.server.routing.route
import io.ktor.server.sse.sse
import io.ktor.sse.ServerSentEvent
import kotlinx.coroutines.flow.filter
import kotlinx.serialization.json.Json
import pt.isel.keepmyplanet.domain.common.Id
import pt.isel.keepmyplanet.dto.message.CreateMessageRequest
import pt.isel.keepmyplanet.mapper.message.toResponse
import pt.isel.keepmyplanet.service.ChatSseService
import pt.isel.keepmyplanet.service.MessageService
import pt.isel.keepmyplanet.utils.getCurrentUserId
import pt.isel.keepmyplanet.utils.getPathIntParameter
import pt.isel.keepmyplanet.utils.getPathUIntId

fun Route.messageWebApi(
    messageService: MessageService,
    chatSseService: ChatSseService,
) {
    route("/events/{eventId}/chat") {
        fun ApplicationCall.getEventId(): Id = getPathUIntId("eventId", "Event ID")

        authenticate("auth-jwt") {
            // Add message to chat
            post {
                val eventId = call.getEventId()
                val senderId = call.getCurrentUserId()
                val request = call.receive<CreateMessageRequest>()

                messageService
                    .addMessage(eventId, senderId, request.content)
                    .onSuccess { call.respond(HttpStatusCode.Created, it.toResponse()) }
                    .onFailure { throw it }
            }

            // Get all messages from chat
            get {
                val eventId = call.getEventId()

                messageService
                    .getAllMessagesFromEvent(eventId)
                    .onSuccess { msg ->
                        call.respond(HttpStatusCode.OK, msg.map { it.toResponse() }.toList())
                    }.onFailure { throw it }
            }

            route("/stream") {
                sse {
                    val eventId = call.getEventId()

                    chatSseService.messages
                        .filter { it.eventId == eventId }
                        .collect { message ->
                            send(
                                ServerSentEvent(
                                    data = Json.encodeToString(message.toResponse()),
                                    id = message.chatPosition.toString(),
                                    event = "new-message",
                                ),
                            )
                        }
                }
            }

            route("/{seq}") {
                fun ApplicationCall.getSequenceNum(): Int =
                    getPathIntParameter(
                        paramName = "seq",
                        description = "Message sequence number",
                    )

                // Get a single message from chat
                get {
                    val eventId = call.getEventId()
                    val sequenceNum = call.getSequenceNum()

                    messageService
                        .getSingleMessageBySequence(eventId, sequenceNum)
                        .onSuccess { call.respond(HttpStatusCode.OK, it.toResponse()) }
                        .onFailure { throw it }
                }

                // Delete a single message from chat
                delete {
                    val eventId = call.getEventId()
                    val sequenceNum = call.getSequenceNum()
                    val requestingUserId = call.getCurrentUserId()

                    messageService
                        .deleteMessage(eventId, sequenceNum, requestingUserId)
                        .onSuccess { call.respond(HttpStatusCode.NoContent) }
                        .onFailure { throw it }
                }
            }
        }
    }
}
