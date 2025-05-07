package pt.isel.keepmyplanet.data.service

import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.delete
import io.ktor.client.request.get
import io.ktor.client.request.header
import io.ktor.client.request.parameter
import io.ktor.client.request.patch
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.contentType
import pt.isel.keepmyplanet.dto.event.CreateEventRequest
import pt.isel.keepmyplanet.dto.event.EventResponse
import pt.isel.keepmyplanet.dto.event.UpdateEventRequest
import pt.isel.keepmyplanet.dto.user.UserResponse

class EventService(
    private val httpClient: HttpClient,
) {
    private object Endpoints {
        const val EVENTS_BASE = "events"

        fun searchEvents() = EVENTS_BASE

        fun createEvent() = EVENTS_BASE

        fun eventById(eventId: UInt) = "$EVENTS_BASE/$eventId"

        fun updateEvent(eventId: UInt) = eventById(eventId)

        fun deleteEvent(eventId: UInt) = eventById(eventId)

        fun cancelEvent(eventId: UInt) = "${eventById(eventId)}/cancel"

        fun completeEvent(eventId: UInt) = "${eventById(eventId)}/complete"

        fun joinEvent(eventId: UInt) = "${eventById(eventId)}/join"

        fun leaveEvent(eventId: UInt) = "${eventById(eventId)}/leave"

        fun getParticipants(eventId: UInt) = "${eventById(eventId)}/participants"
    }

    suspend fun searchAllEvents(query: String?): Result<List<EventResponse>> =
        runCatching {
            httpClient
                .get(Endpoints.searchEvents()) {
                    if (query != null) {
                        parameter("name", query)
                    }
                }.body<List<EventResponse>>()
        }

    suspend fun createEvent(
        request: CreateEventRequest,
        organizerId: UInt,
    ): Result<EventResponse> =
        runCatching {
            httpClient
                .post(Endpoints.createEvent()) {
                    contentType(ContentType.Application.Json)
                    setBody(request)
                    header("X-Mock-User-Id", organizerId.toString())
                }.body<EventResponse>()
        }

    suspend fun getEventDetails(eventId: UInt): Result<EventResponse> =
        runCatching {
            httpClient.get(Endpoints.eventById(eventId)).body<EventResponse>()
        }

    suspend fun updateEventDetails(
        eventId: UInt,
        userId: UInt,
        request: UpdateEventRequest,
    ): Result<EventResponse> =
        runCatching {
            httpClient
                .patch(Endpoints.updateEvent(eventId)) {
                    contentType(ContentType.Application.Json)
                    setBody(request)
                    header("X-Mock-User-Id", userId.toString())
                }.body<EventResponse>()
        }

    suspend fun deleteEvent(
        eventId: UInt,
        userId: UInt,
    ): Result<Unit> =
        runCatching {
            httpClient.delete(Endpoints.deleteEvent(eventId)) {
                header("X-Mock-User-Id", userId.toString())
            }
            Unit
        }

    suspend fun cancelEvent(
        eventId: UInt,
        userId: UInt,
    ): Result<EventResponse> =
        runCatching {
            httpClient
                .post(Endpoints.cancelEvent(eventId)) {
                    header("X-Mock-User-Id", userId.toString())
                }.body<EventResponse>()
        }

    suspend fun completeEvent(
        eventId: UInt,
        userId: UInt,
    ): Result<EventResponse> =
        runCatching {
            httpClient
                .post(Endpoints.completeEvent(eventId)) {
                    header("X-Mock-User-Id", userId.toString())
                }.body<EventResponse>()
        }

    suspend fun joinEvent(
        eventId: UInt,
        userId: UInt,
    ): Result<EventResponse> =
        runCatching {
            httpClient
                .post(Endpoints.joinEvent(eventId)) {
                    header("X-Mock-User-Id", userId.toString())
                }.body<EventResponse>()
        }

    suspend fun leaveEvent(
        eventId: UInt,
        userId: UInt,
    ): Result<EventResponse> =
        runCatching {
            httpClient
                .post(Endpoints.leaveEvent(eventId)) {
                    header("X-Mock-User-Id", userId.toString())
                }.body<EventResponse>()
        }

    suspend fun getEventParticipants(eventId: UInt): Result<List<UserResponse>> =
        runCatching {
            httpClient.get(Endpoints.getParticipants(eventId)).body<List<UserResponse>>()
        }
}
