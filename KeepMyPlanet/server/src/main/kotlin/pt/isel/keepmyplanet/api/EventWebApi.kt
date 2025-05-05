@file:Suppress("ktlint:standard:no-wildcard-imports")

package pt.isel.keepmyplanet.api

import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import kotlinx.datetime.LocalDateTime
import pt.isel.keepmyplanet.domain.common.Id
import pt.isel.keepmyplanet.dto.event.ChangeEventStatusRequest
import pt.isel.keepmyplanet.dto.event.CreateEventRequest
import pt.isel.keepmyplanet.mapper.event.toResponse
import pt.isel.keepmyplanet.service.EventService
import pt.isel.keepmyplanet.service.EventStateChangeService
import pt.isel.keepmyplanet.util.getPathUIntId

fun Route.eventWebApi(
    eventService: EventService,
    eventStateChangeService: EventStateChangeService,
) {
    get("/event") {
        val query = call.request.queryParameters["name"]

        eventService
            .searchAllEvents(query)
            .onSuccess { events -> call.respond(events.map { it.toResponse() }) }
            .onFailure { throw it }
    }

    route("/zone/{zoneId}/event") {
        // Create Event
        post {
            val zoneId = call.getPathUIntId("zoneId", "Zone ID")
            val organizerId = getCurrentUserId() // hardcoded
            val request = call.receive<CreateEventRequest>()
            val periodStart = LocalDateTime.parse(request.periodStart)
            val periodEnd = LocalDateTime.parse(request.periodEnd)

            eventService
                .createEvent(
                    zoneId,
                    organizerId,
                    request.title,
                    request.description,
                    periodStart,
                    periodEnd,
                    request.maxParticipants,
                ).onSuccess { call.respond(HttpStatusCode.Created, it.toResponse()) }
                .onFailure { throw it }
        }

        // Search Events (Optional - by name)
        get {
            val zoneId = call.getPathUIntId("zoneId", "Zone ID")
            val query = call.request.queryParameters["name"]

            eventService
                .searchZoneEvents(zoneId, query)
                .onSuccess { events -> call.respond(events.map { it.toResponse() }) }
                .onFailure { throw it }
        }

        route("/{eventId}") {
            fun ApplicationCall.getEventId(): Id = getPathUIntId("eventId", "Event ID")

            // Event Details
            get {
                val eventId = call.getEventId()

                eventService
                    .getEventDetails(eventId)
                    .onSuccess { call.respond(it.toResponse()) }
                    .onFailure { throw it }
            }

            // Join Event
            post("/join") {
                val eventId = call.getEventId()
                val userId = getCurrentUserId()

                eventService
                    .joinEvent(eventId, userId)
                    .onSuccess { call.respond(HttpStatusCode.OK) }
                    .onFailure { throw it }
            }

            // Change Event Status
            put("/status") {
                val eventId = call.getEventId()
                val userId = getCurrentUserId()
                val request = call.receive<ChangeEventStatusRequest>()

                eventStateChangeService
                    .changeEventStatus(eventId, request.newStatus, userId)
                    .onSuccess { call.respond(HttpStatusCode.OK, it.toResponse()) }
                    .onFailure { throw it }
            }

            // Show Event Status History
            get("/status/history") {
                val eventId = call.getEventId()
                println("Ver histórico para eventId = $eventId")
                eventStateChangeService
                    .getEventStateChanges(eventId)
                    .onSuccess { changes -> call.respond(changes.map { it.toResponse() }) }
                    .onFailure {
                        println("Erro ao obter histórico de eventos: ${it.message}")
                        throw it
                    }
            }
        }
    }
}
