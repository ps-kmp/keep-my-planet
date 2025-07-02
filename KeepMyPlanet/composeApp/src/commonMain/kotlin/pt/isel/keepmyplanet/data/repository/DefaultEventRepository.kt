package pt.isel.keepmyplanet.data.repository

import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import pt.isel.keepmyplanet.data.api.EventApi
import pt.isel.keepmyplanet.data.cache.EventCacheRepository
import pt.isel.keepmyplanet.data.cache.EventStatusHistoryCacheRepository
import pt.isel.keepmyplanet.data.cache.UserCacheRepository
import pt.isel.keepmyplanet.domain.common.EventDetailsBundle
import pt.isel.keepmyplanet.domain.common.Id
import pt.isel.keepmyplanet.domain.event.Event
import pt.isel.keepmyplanet.domain.event.EventFilterType
import pt.isel.keepmyplanet.domain.event.EventListItem
import pt.isel.keepmyplanet.domain.user.UserInfo
import pt.isel.keepmyplanet.dto.event.ChangeEventStatusRequest
import pt.isel.keepmyplanet.dto.event.CheckInRequest
import pt.isel.keepmyplanet.dto.event.CreateEventRequest
import pt.isel.keepmyplanet.dto.event.EventResponse
import pt.isel.keepmyplanet.dto.event.EventStateChangeResponse
import pt.isel.keepmyplanet.dto.event.InitiateTransferRequest
import pt.isel.keepmyplanet.dto.event.RespondToTransferRequest
import pt.isel.keepmyplanet.dto.event.UpdateEventRequest
import pt.isel.keepmyplanet.mapper.event.toEvent
import pt.isel.keepmyplanet.mapper.event.toListItem
import pt.isel.keepmyplanet.mapper.user.toUserCacheInfo
import pt.isel.keepmyplanet.mapper.user.toUserInfo

class DefaultEventRepository(
    private val eventApi: EventApi,
    private val eventCache: EventCacheRepository,
    private val userCache: UserCacheRepository,
    private val historyCache: EventStatusHistoryCacheRepository,
) {
    suspend fun getEventDetails(eventId: Id): Result<Event> =
        runCatching {
            val networkResult = eventApi.getEventDetails(eventId.value).map { it.toEvent() }
            if (networkResult.isSuccess) {
                val event = networkResult.getOrThrow()
                eventCache.insertEvents(listOf(event))
                event
            } else {
                eventCache.getEventById(eventId) ?: throw networkResult.exceptionOrNull()!!
            }
        }

    suspend fun getEventDetailsBundle(eventId: Id): Result<EventDetailsBundle> =
        runCatching {
            println("DEBUG: [1] Starting getEventDetailsBundle for event $eventId")
            coroutineScope {
                val detailsDeferred = async { eventApi.getEventDetails(eventId.value) }
                val participantsDeferred = async { eventApi.getEventParticipants(eventId.value) }

                println("DEBUG: [2] Waiting for API calls to complete...")

                val eventDetailsResult = detailsDeferred.await()
                println("DEBUG: [3] eventApi.getEventDetails finished with success=${eventDetailsResult.isSuccess}")
                val eventResponse = eventDetailsResult.getOrThrow()

                val participantsResult = participantsDeferred.await()
                println("DEBUG: [4] eventApi.getEventParticipants finished with success=${participantsResult.isSuccess}")
                val participantsResponse = participantsResult.getOrThrow()

                println("DEBUG: [5] Mapping responses to domain objects...")
                println("DEBUG: Raw EventResponse from API: $eventResponse")

                val event = eventResponse.toEvent()
                val participants = participantsResponse.map { it.toUserInfo() }

                println("DEBUG: Mapped Event object: $event")
                println("DEBUG: Mapped Event has pendingOrganizerId = ${event.pendingOrganizerId}")


                println("DEBUG: [6] Inserting into cache...")
                eventCache.insertEvents(listOf(event))
                userCache.insertUsers(participants.map { it.toUserCacheInfo() })
                println("DEBUG: [7] Finished inserting into cache. Returning success bundle.")

                EventDetailsBundle(event, participants)
            }
        }.recoverCatching { throwable ->
            println("DEBUG: [ERROR] Entered recoverCatching. The error was: $throwable")

            val cachedEvent = eventCache.getEventById(eventId)
            println("DEBUG: [CACHE] Trying to use cache. Found event: $cachedEvent")

            if (cachedEvent != null) {
                val cachedParticipants =
                    userCache.getUsersByIds(cachedEvent.participantsIds.toList())
                if (cachedParticipants.size == cachedEvent.participantsIds.size) {
                    println("DEBUG: [CACHE] Successfully created bundle from cache.")
                    EventDetailsBundle(cachedEvent, cachedParticipants.map { it.toUserInfo() })
                } else {
                    println("DEBUG: [CACHE] Found event but participants are missing. Re-throwing error.")
                    throw throwable
                }
            } else {
                println("DEBUG: [CACHE] Event not found in cache. Re-throwing error.")
                throw throwable
            }
        }

    suspend fun invalidateEventCache(eventId: Id) {
        eventCache.deleteEventById(eventId)
    }

    suspend fun getEventParticipants(eventId: Id): Result<List<UserInfo>> =
        runCatching {
            val result = eventApi.getEventParticipants(eventId.value)
            val participants = result.getOrThrow().map { it.toUserInfo() }
            userCache.insertUsers(participants.map { it.toUserCacheInfo() })
            participants
        }.recoverCatching {
            userCache
                .getUsersByIds(
                    eventCache.getEventById(eventId)?.participantsIds?.toList() ?: emptyList(),
                ).map { it.toUserInfo() }
        }

    suspend fun getEventAttendees(eventId: Id): Result<List<UserInfo>> =
        eventApi.getEventAttendees(eventId.value).map { userResponses ->
            userResponses.map { it.toUserInfo() }
        }

    suspend fun getEventStatusHistory(eventId: Id): Result<List<EventStateChangeResponse>> =
        runCatching {
            val result = eventApi.getEventStatusHistory(eventId.value)
            val history = result.getOrThrow()
            historyCache.insertHistory(eventId, history)
            history
        }.recoverCatching {
            historyCache.getHistoryByEventId(eventId)
        }

    suspend fun searchEvents(
        filter: EventFilterType,
        query: String?,
        limit: Int,
        offset: Int,
    ): Result<List<EventListItem>> {
        val apiCall: suspend () -> Result<List<EventResponse>> = {
            when (filter) {
                EventFilterType.ALL -> eventApi.searchAllEvents(query, limit, offset)
                EventFilterType.ORGANIZED -> eventApi.searchOrganizedEvents(query, limit, offset)
                EventFilterType.JOINED -> eventApi.searchJoinedEvents(query, limit, offset)
            }
        }
        return runCatching {
            val result = apiCall()
            val events = result.getOrThrow().map { it.toListItem() }
            if (offset == 0) {
                eventCache.insertEvents(result.getOrThrow().map { it.toEvent() })
            }
            events
        }.recoverCatching {
            if (offset == 0) {
                eventCache.getAllEvents().map { it.toListItem() }
            } else {
                emptyList()
            }
        }
    }

    suspend fun createEvent(request: CreateEventRequest): Result<Event> =
        eventApi.createEvent(request).map { it.toEvent() }

    suspend fun updateEvent(
        eventId: Id,
        request: UpdateEventRequest,
    ): Result<Event> = eventApi.updateEventDetails(eventId.value, request).map { it.toEvent() }

    suspend fun deleteEvent(eventId: Id): Result<Unit> = eventApi.deleteEvent(eventId.value)

    suspend fun joinEvent(eventId: Id): Result<Event> =
        eventApi.joinEvent(eventId.value).map { it.toEvent() }

    suspend fun leaveEvent(eventId: Id): Result<Event> =
        eventApi.leaveEvent(eventId.value).map { it.toEvent() }

    suspend fun checkInUser(
        eventId: Id,
        request: CheckInRequest,
    ): Result<Unit> = eventApi.checkInUser(eventId.value, request)

    suspend fun changeEventStatus(
        eventId: Id,
        request: ChangeEventStatusRequest,
    ): Result<Event> = eventApi.changeEventStatus(eventId.value, request).map { it.toEvent() }

    suspend fun getAttendedEvents(
        limit: Int,
        offset: Int,
    ): Result<List<EventListItem>> =
        eventApi.getAttendedEvents(limit, offset).map { list -> list.map { it.toListItem() } }

    suspend fun initiateTransfer(
        eventId: Id,
        nomineeId: Id
    ): Result<Event> {
        val request = InitiateTransferRequest(nomineeId.value)
        return eventApi.initiateTransfer(eventId.value, request).map {
            val updatedEvent = it.toEvent()
            eventCache.insertEvents(listOf(updatedEvent))
            updatedEvent
        }
    }

    suspend fun respondToTransfer(
        eventId: Id,
        accepted: Boolean
    ): Result<Event> {
        val request = RespondToTransferRequest(accept = accepted)
        return eventApi.respondToTransfer(eventId.value, request).map {
            val updatedEvent = it.toEvent()
            eventCache.insertEvents(listOf(updatedEvent))
            updatedEvent
        }
    }
}
