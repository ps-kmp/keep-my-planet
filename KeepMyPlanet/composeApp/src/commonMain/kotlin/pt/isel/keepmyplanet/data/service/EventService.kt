package pt.isel.keepmyplanet.data.service

import io.ktor.client.HttpClient
import io.ktor.client.request.parameter
import io.ktor.client.request.setBody
import io.ktor.client.request.url
import io.ktor.http.HttpMethod
import pt.isel.keepmyplanet.data.api.executeRequest
import pt.isel.keepmyplanet.data.api.executeRequestUnit
import pt.isel.keepmyplanet.data.api.mockUser
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
        httpClient.executeRequest {
            method = HttpMethod.Get
            url(Endpoints.searchEvents())
            if (query != null) parameter("name", query)
        }

    suspend fun createEvent(
        request: CreateEventRequest,
        organizerId: UInt,
    ): Result<EventResponse> =
        httpClient.executeRequest {
            method = HttpMethod.Post
            url(Endpoints.createEvent())
            setBody(request)
            mockUser(organizerId)
        }

    suspend fun getEventDetails(eventId: UInt): Result<EventResponse> =
        httpClient.executeRequest {
            method = HttpMethod.Get
            url(Endpoints.eventById(eventId))
        }

    suspend fun updateEventDetails(
        eventId: UInt,
        userId: UInt,
        request: UpdateEventRequest,
    ): Result<EventResponse> =
        httpClient.executeRequest {
            method = HttpMethod.Patch
            url(Endpoints.updateEvent(eventId))
            setBody(request)
            mockUser(userId)
        }

    suspend fun deleteEvent(
        eventId: UInt,
        userId: UInt,
    ): Result<Unit> =
        httpClient.executeRequestUnit {
            method = HttpMethod.Delete
            url(Endpoints.deleteEvent(eventId))
            mockUser(userId)
        }

    suspend fun cancelEvent(
        eventId: UInt,
        userId: UInt,
    ): Result<EventResponse> =
        httpClient.executeRequest {
            method = HttpMethod.Post
            url(Endpoints.cancelEvent(eventId))
            mockUser(userId)
        }

    suspend fun completeEvent(
        eventId: UInt,
        userId: UInt,
    ): Result<EventResponse> =
        httpClient.executeRequest {
            method = HttpMethod.Post
            url(Endpoints.completeEvent(eventId))
            mockUser(userId)
        }

    suspend fun joinEvent(
        eventId: UInt,
        userId: UInt,
    ): Result<EventResponse> =
        httpClient.executeRequest {
            method = HttpMethod.Post
            url(Endpoints.joinEvent(eventId))
            mockUser(userId)
        }

    suspend fun leaveEvent(
        eventId: UInt,
        userId: UInt,
    ): Result<EventResponse> =
        httpClient.executeRequest {
            method = HttpMethod.Post
            url(Endpoints.leaveEvent(eventId))
            mockUser(userId)
        }

    suspend fun getEventParticipants(eventId: UInt): Result<List<UserResponse>> =
        httpClient.executeRequest {
            method = HttpMethod.Get
            url(Endpoints.getParticipants(eventId))
        }
}
