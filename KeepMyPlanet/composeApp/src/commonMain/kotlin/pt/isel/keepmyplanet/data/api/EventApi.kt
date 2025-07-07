package pt.isel.keepmyplanet.data.api

import io.ktor.client.HttpClient
import io.ktor.client.request.parameter
import io.ktor.client.request.setBody
import io.ktor.client.request.url
import io.ktor.http.HttpMethod
import pt.isel.keepmyplanet.data.http.executeRequest
import pt.isel.keepmyplanet.data.http.executeRequestUnit
import pt.isel.keepmyplanet.dto.event.ChangeEventStatusRequest
import pt.isel.keepmyplanet.dto.event.CheckInRequest
import pt.isel.keepmyplanet.dto.event.CreateEventRequest
import pt.isel.keepmyplanet.dto.event.EventResponse
import pt.isel.keepmyplanet.dto.event.EventStateChangeResponse
import pt.isel.keepmyplanet.dto.event.EventStatsResponse
import pt.isel.keepmyplanet.dto.event.InitiateTransferRequest
import pt.isel.keepmyplanet.dto.event.ManualNotificationRequest
import pt.isel.keepmyplanet.dto.event.RespondToTransferRequest
import pt.isel.keepmyplanet.dto.event.UpdateEventRequest
import pt.isel.keepmyplanet.dto.user.UserResponse

class EventApi(
    private val httpClient: HttpClient,
) {
    private object Endpoints {
        const val EVENTS_BASE = "events"

        fun searchEvents() = EVENTS_BASE

        fun organizedEvents() = "$EVENTS_BASE/organized"

        fun joinedEvents() = "$EVENTS_BASE/joined"

        fun createEvent() = EVENTS_BASE

        fun eventById(eventId: UInt) = "$EVENTS_BASE/$eventId"

        fun updateEvent(eventId: UInt) = eventById(eventId)

        fun deleteEvent(eventId: UInt) = eventById(eventId)

        fun changeStatus(eventId: UInt) = "${eventById(eventId)}/status"

        fun getStatusHistory(eventId: UInt) = "${eventById(eventId)}/status/history"

        fun joinEvent(eventId: UInt) = "${eventById(eventId)}/join"

        fun leaveEvent(eventId: UInt) = "${eventById(eventId)}/leave"

        fun getParticipants(eventId: UInt) = "${eventById(eventId)}/participants"

        fun getAttendees(eventId: UInt) = "${eventById(eventId)}/attendees"

        fun checkInUser(eventId: UInt) = "${eventById(eventId)}/check-in"

        fun attendedEvents() = "$EVENTS_BASE/attended"

        fun initiateTransfer(eventId: UInt) = "${eventById(eventId)}/initiate-transfer"

        fun respondToTransfer(eventId: UInt) = "${eventById(eventId)}/respond-to-transfer"

        fun getEventStats(eventId: UInt) = "${eventById(eventId)}/stats"

        fun notifyParticipants(eventId: UInt) = "${eventById(eventId)}/notify"
    }

    suspend fun searchAllEvents(
        query: String?,
        limit: Int,
        offset: Int,
    ): Result<List<EventResponse>> =
        httpClient.executeRequest {
            method = HttpMethod.Get
            url(Endpoints.searchEvents())
            if (query != null) parameter("name", query)
            parameter("limit", limit)
            parameter("offset", offset)
        }

    suspend fun searchOrganizedEvents(
        query: String?,
        limit: Int,
        offset: Int,
    ): Result<List<EventResponse>> =
        httpClient.executeRequest {
            method = HttpMethod.Get
            url(Endpoints.organizedEvents())
            if (query != null) parameter("name", query)
            parameter("limit", limit)
            parameter("offset", offset)
        }

    suspend fun searchJoinedEvents(
        query: String?,
        limit: Int,
        offset: Int,
    ): Result<List<EventResponse>> =
        httpClient.executeRequest {
            method = HttpMethod.Get
            url(Endpoints.joinedEvents())
            if (query != null) parameter("name", query)
            parameter("limit", limit)
            parameter("offset", offset)
        }

    suspend fun createEvent(request: CreateEventRequest): Result<EventResponse> =
        httpClient.executeRequest {
            method = HttpMethod.Post
            url(Endpoints.createEvent())
            setBody(request)
        }

    suspend fun getEventDetails(eventId: UInt): Result<EventResponse> =
        httpClient.executeRequest {
            method = HttpMethod.Get
            url(Endpoints.eventById(eventId))
        }

    suspend fun updateEventDetails(
        eventId: UInt,
        request: UpdateEventRequest,
    ): Result<EventResponse> =
        httpClient.executeRequest {
            method = HttpMethod.Patch
            url(Endpoints.updateEvent(eventId))
            setBody(request)
        }

    suspend fun deleteEvent(eventId: UInt): Result<Unit> =
        httpClient.executeRequestUnit {
            method = HttpMethod.Delete
            url(Endpoints.deleteEvent(eventId))
        }

    suspend fun changeEventStatus(
        eventId: UInt,
        request: ChangeEventStatusRequest,
    ): Result<EventResponse> =
        httpClient.executeRequest {
            method = HttpMethod.Put
            url(Endpoints.changeStatus(eventId))
            setBody(request)
        }

    suspend fun getEventStatusHistory(eventId: UInt): Result<List<EventStateChangeResponse>> =
        httpClient.executeRequest {
            method = HttpMethod.Get
            url(Endpoints.getStatusHistory(eventId))
        }

    suspend fun joinEvent(eventId: UInt): Result<EventResponse> =
        httpClient.executeRequest {
            method = HttpMethod.Post
            url(Endpoints.joinEvent(eventId))
        }

    suspend fun leaveEvent(eventId: UInt): Result<EventResponse> =
        httpClient.executeRequest {
            method = HttpMethod.Post
            url(Endpoints.leaveEvent(eventId))
        }

    suspend fun getEventParticipants(eventId: UInt): Result<List<UserResponse>> =
        httpClient.executeRequest {
            method = HttpMethod.Get
            url(Endpoints.getParticipants(eventId))
        }

    suspend fun getEventAttendees(eventId: UInt): Result<List<UserResponse>> =
        httpClient.executeRequest {
            method = HttpMethod.Get
            url(Endpoints.getAttendees(eventId))
        }

    suspend fun checkInUser(
        eventId: UInt,
        request: CheckInRequest,
    ): Result<Unit> =
        httpClient.executeRequestUnit {
            method = HttpMethod.Post
            url(Endpoints.checkInUser(eventId))
            setBody(request)
        }

    suspend fun getAttendedEvents(
        limit: Int,
        offset: Int,
    ): Result<List<EventResponse>> =
        httpClient.executeRequest {
            method = HttpMethod.Get
            url(Endpoints.attendedEvents())
            parameter("limit", limit)
            parameter("offset", offset)
        }

    suspend fun initiateTransfer(
        eventId: UInt,
        request: InitiateTransferRequest,
    ): Result<EventResponse> =
        httpClient.executeRequest {
            method = HttpMethod.Post
            url(Endpoints.initiateTransfer(eventId))
            setBody(request)
        }

    suspend fun respondToTransfer(
        eventId: UInt,
        request: RespondToTransferRequest,
    ): Result<EventResponse> =
        httpClient.executeRequest {
            method = HttpMethod.Post
            url(Endpoints.respondToTransfer(eventId))
            setBody(request)
        }

    suspend fun getEventStats(eventId: UInt): Result<EventStatsResponse> =
        httpClient.executeRequest {
            method = HttpMethod.Get
            url(Endpoints.getEventStats(eventId))
        }

    suspend fun sendManualNotification(
        eventId: UInt,
        request: ManualNotificationRequest,
    ): Result<Unit> =
        httpClient.executeRequestUnit {
            method = HttpMethod.Post
            url(Endpoints.notifyParticipants(eventId))
            setBody(request)
        }
}
