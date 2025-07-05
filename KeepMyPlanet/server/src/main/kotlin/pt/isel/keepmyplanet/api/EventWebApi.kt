package pt.isel.keepmyplanet.api

import io.ktor.http.HttpStatusCode
import io.ktor.server.application.ApplicationCall
import io.ktor.server.auth.authenticate
import io.ktor.server.plugins.BadRequestException
import io.ktor.server.request.receive
import io.ktor.server.response.respond
import io.ktor.server.routing.Route
import io.ktor.server.routing.delete
import io.ktor.server.routing.get
import io.ktor.server.routing.patch
import io.ktor.server.routing.post
import io.ktor.server.routing.put
import io.ktor.server.routing.route
import kotlinx.datetime.LocalDateTime
import pt.isel.keepmyplanet.domain.common.Description
import pt.isel.keepmyplanet.domain.common.Id
import pt.isel.keepmyplanet.domain.event.Period
import pt.isel.keepmyplanet.domain.event.Title
import pt.isel.keepmyplanet.dto.event.ChangeEventStatusRequest
import pt.isel.keepmyplanet.dto.event.CheckInRequest
import pt.isel.keepmyplanet.dto.event.CreateEventRequest
import pt.isel.keepmyplanet.dto.event.InitiateTransferRequest
import pt.isel.keepmyplanet.dto.event.RespondToTransferRequest
import pt.isel.keepmyplanet.dto.event.UpdateEventRequest
import pt.isel.keepmyplanet.mapper.event.toResponse
import pt.isel.keepmyplanet.mapper.user.toResponse
import pt.isel.keepmyplanet.service.EventService
import pt.isel.keepmyplanet.service.EventStateChangeService
import pt.isel.keepmyplanet.utils.getCurrentUserId
import pt.isel.keepmyplanet.utils.getPathUIntId
import pt.isel.keepmyplanet.utils.getQueryIntParameter
import pt.isel.keepmyplanet.utils.getQueryStringParameter

fun Route.eventWebApi(
    eventService: EventService,
    eventStateChangeService: EventStateChangeService,
) {
    route("/events") {
        // Search System Events (Optional - by name)
        get {
            val query = call.getQueryStringParameter("name")
            val limit = call.getQueryIntParameter("limit", default = 10)
            val offset = call.getQueryIntParameter("offset", default = 0)

            if (limit < 1 || limit > 100) {
                throw BadRequestException("Limit must be between 1 and 100.")
            }
            if (offset < 0) {
                throw BadRequestException("Offset must be non-negative.")
            }

            eventService
                .searchAllEvents(query, limit, offset)
                .onSuccess { events ->
                    call.respond(HttpStatusCode.OK, events.map { it.toResponse() })
                }.onFailure { throw it }
        }

        authenticate("auth-jwt") {
            // Get events organized by the current user
            get("/organized") {
                val userId = call.getCurrentUserId()
                val limit = call.getQueryIntParameter("limit", default = 10)
                val offset = call.getQueryIntParameter("offset", default = 0)
                val query = call.getQueryStringParameter("name")
                eventService
                    .getEventsOrganizedBy(userId, query, limit, offset)
                    .onSuccess { events ->
                        call.respond(HttpStatusCode.OK, events.map { it.toResponse() })
                    }.onFailure { throw it }
            }

            get("/attended") {
                val userId = call.getCurrentUserId()
                val limit = call.getQueryIntParameter("limit", default = 10)
                val offset = call.getQueryIntParameter("offset", default = 0)

                eventService
                    .getEventsAttendedBy(userId, limit, offset)
                    .onSuccess { events ->
                        call.respond(HttpStatusCode.OK, events.map { it.toResponse() })
                    }.onFailure { throw it }
            }

            // Get events the current user has joined
            get("/joined") {
                val userId = call.getCurrentUserId()
                val limit = call.getQueryIntParameter("limit", default = 10)
                val offset = call.getQueryIntParameter("offset", default = 0)
                val query = call.getQueryStringParameter("name")
                eventService
                    .getEventsJoinedBy(userId, query, limit, offset)
                    .onSuccess { events ->
                        call.respond(HttpStatusCode.OK, events.map { it.toResponse() })
                    }.onFailure { throw it }
            }

            // Create Event
            post {
                val request = call.receive<CreateEventRequest>()
                val organizerId = call.getCurrentUserId()

                val title = Title(request.title)
                val description = Description(request.description)
                val period =
                    Period(
                        LocalDateTime.parse(request.startDate),
                        request.endDate?.let { LocalDateTime.parse(it) },
                    )
                val zoneId = Id(request.zoneId)
                val maxParticipants = request.maxParticipants

                eventService
                    .createEvent(title, description, period, zoneId, organizerId, maxParticipants)
                    .onSuccess { event -> call.respond(HttpStatusCode.Created, event.toResponse()) }
                    .onFailure { throw it }
            }
        }

        route("/{id}") {
            fun ApplicationCall.getEventId(): Id = getPathUIntId("id", "Event ID")

            // Get Event Details
            get {
                val eventId = call.getEventId()

                eventService
                    .getEventDetails(eventId)
                    .onSuccess { event -> call.respond(HttpStatusCode.OK, event.toResponse()) }
                    .onFailure { throw it }
            }

            get("/attendees") {
                val eventId = call.getEventId()

                eventService
                    .getEventAttendees(eventId)
                    .onSuccess { users ->
                        call.respond(HttpStatusCode.OK, users.map { it.toResponse() })
                    }.onFailure { throw it }
            }

            authenticate("auth-jwt") {
                // Update Event Details
                patch {
                    val eventId = call.getEventId()
                    val userId = call.getCurrentUserId()
                    val request = call.receive<UpdateEventRequest>()

                    val title = request.title?.let { Title(it) }
                    val description = request.description?.let { Description(it) }
                    val period =
                        request.startDate?.let {
                            Period(
                                LocalDateTime.parse(it),
                                request.endDate?.let { e -> LocalDateTime.parse(e) },
                            )
                        }
                    val max = request.maxParticipants

                    eventService
                        .updateEventDetails(eventId, userId, title, description, period, max)
                        .onSuccess { event -> call.respond(HttpStatusCode.OK, event.toResponse()) }
                        .onFailure { throw it }
                }

                // Delete Event
                delete {
                    val eventId = call.getEventId()
                    val userId = call.getCurrentUserId()

                    eventService
                        .deleteEvent(eventId, userId)
                        .onSuccess { call.respond(HttpStatusCode.NoContent) }
                        .onFailure { throw it }
                }

                post("/check-in") {
                    val eventId = call.getEventId()
                    val actingUserId = call.getCurrentUserId()
                    val request = call.receive<CheckInRequest>()
                    val userIdToCheckIn = Id(request.userId)

                    eventService
                        .checkInUserToEvent(eventId, userIdToCheckIn, actingUserId)
                        .onSuccess {
                            call.respond(
                                HttpStatusCode.OK,
                                mapOf("message" to "User checked in successfully."),
                            )
                        }.onFailure { throw it }
                }

                put("/status") {
                    val eventId = call.getEventId()
                    val userId = call.getCurrentUserId()
                    val request = call.receive<ChangeEventStatusRequest>()

                    eventStateChangeService
                        .changeEventStatus(eventId, request.newStatus, userId)
                        .onSuccess { call.respond(HttpStatusCode.OK, it.toResponse()) }
                        .onFailure { throw it }
                }

                // Join Event
                post("/join") {
                    val eventId = call.getEventId()
                    val userId = call.getCurrentUserId()

                    eventService
                        .joinEvent(eventId, userId)
                        .onSuccess { event -> call.respond(HttpStatusCode.OK, event.toResponse()) }
                        .onFailure { throw it }
                }

                // Leave Event
                post("/leave") {
                    val eventId = call.getEventId()
                    val userId = call.getCurrentUserId()

                    eventService
                        .leaveEvent(eventId, userId)
                        .onSuccess { event -> call.respond(HttpStatusCode.OK, event.toResponse()) }
                        .onFailure { throw it }
                }

                post("/initiate-transfer") {
                    val eventId = call.getEventId()
                    val currentOrganizerId = call.getCurrentUserId()
                    val request = call.receive<InitiateTransferRequest>()
                    val nomineeId = Id(request.nomineeId)

                    eventService
                        .initiateTransfer(eventId, currentOrganizerId, nomineeId)
                        .onSuccess { call.respond(HttpStatusCode.OK, it.toResponse()) }
                        .onFailure { throw it }
                }

                post("/respond-to-transfer") {
                    val eventId = call.getEventId()
                    val nomineeId = call.getCurrentUserId()
                    val request = call.receive<RespondToTransferRequest>()

                    eventService
                        .respondToTransfer(eventId, nomineeId, request.accept)
                        .onSuccess { call.respond(HttpStatusCode.OK, it.toResponse()) }
                        .onFailure { throw it }
                }
            }

            // Get Event Participants
            get("/participants") {
                val eventId = call.getEventId()

                eventService
                    .getEventParticipants(eventId)
                    .onSuccess { users ->
                        call.respond(HttpStatusCode.OK, users.map { it.toResponse() })
                    }.onFailure { throw it }
            }

            // Show Event Status History
            get("/status/history") {
                val eventId = call.getEventId()

                eventStateChangeService
                    .getEventStateChanges(eventId)
                    .onSuccess { changes ->
                        call.respond(HttpStatusCode.OK, changes)
                    }.onFailure { throw it }
            }
        }
    }
}
