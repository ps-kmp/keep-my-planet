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
    private val eventCache: EventCacheRepository?,
    private val userCache: UserCacheRepository?,
    private val historyCache: EventStatusHistoryCacheRepository?,
) {
    suspend fun getEventDetails(eventId: Id): Result<Event> =
        runCatching {
            eventCache?.getEventById(eventId)?.let {
                return@runCatching it
            }

            val networkEvent = eventApi.getEventDetails(eventId.value).getOrThrow().toEvent()
            eventCache?.insertEvents(listOf(networkEvent))
            networkEvent
        }.recoverCatching {
            eventCache?.getEventById(eventId) ?: throw it
        }

    suspend fun getEventDetailsBundle(eventId: Id): Result<EventDetailsBundle> =
        runCatching {
            val cachedEvent = eventCache?.getEventById(eventId)
            if (cachedEvent != null) {
                val cachedParticipants =
                    userCache?.getUsersByIds(cachedEvent.participantsIds.toList())
                if (cachedParticipants != null &&
                    cachedParticipants.size == cachedEvent.participantsIds.size
                ) {
                    return@runCatching EventDetailsBundle(
                        cachedEvent,
                        cachedParticipants.map { it.toUserInfo() },
                    )
                }
            }

            coroutineScope {
                val detailsDeferred = async { eventApi.getEventDetails(eventId.value) }
                val participantsDeferred = async { eventApi.getEventParticipants(eventId.value) }

                val eventDetailsResult = detailsDeferred.await()
                val eventResponse = eventDetailsResult.getOrThrow()

                val participantsResult = participantsDeferred.await()
                val participantsResponse = participantsResult.getOrThrow()

                val event = eventResponse.toEvent()
                val participants = participantsResponse.map { it.toUserInfo() }

                eventCache?.insertEvents(listOf(event))
                userCache?.insertUsers(participants.map { it.toUserCacheInfo() })

                EventDetailsBundle(event, participants)
            }
        }.recoverCatching { throwable ->
            val cachedEvent = eventCache?.getEventById(eventId)

            if (cachedEvent != null) {
                val cachedParticipants =
                    userCache?.getUsersByIds(cachedEvent.participantsIds.toList())
                if (cachedParticipants != null &&
                    cachedParticipants.size == cachedEvent.participantsIds.size
                ) {
                    EventDetailsBundle(cachedEvent, cachedParticipants.map { it.toUserInfo() })
                } else {
                    throw throwable
                }
            } else {
                throw throwable
            }
        }

    suspend fun invalidateEventCache(eventId: Id) {
        eventCache?.deleteEventById(eventId)
    }

    suspend fun getEventParticipants(eventId: Id): Result<List<UserInfo>> =
        runCatching {
            val event = eventCache?.getEventById(eventId)
            if (event != null) {
                val participants = userCache?.getUsersByIds(event.participantsIds.toList())
                if (participants != null && participants.size == event.participantsIds.size) {
                    return@runCatching participants.map { it.toUserInfo() }
                }
            }

            val networkParticipants =
                eventApi.getEventParticipants(eventId.value).getOrThrow().map { it.toUserInfo() }
            userCache?.insertUsers(networkParticipants.map { it.toUserCacheInfo() })
            networkParticipants
        }.recoverCatching { throwable ->
            val event = eventCache?.getEventById(eventId)
            if (event != null) {
                userCache
                    ?.getUsersByIds(event.participantsIds.toList())
                    ?.map { it.toUserInfo() }
                    ?: throw throwable
            } else {
                throw throwable
            }
        }

    suspend fun getEventAttendees(eventId: Id): Result<List<UserInfo>> =
        eventApi.getEventAttendees(eventId.value).map { userResponses ->
            userResponses.map { it.toUserInfo() }
        }

    suspend fun getEventStatusHistory(eventId: Id): Result<List<EventStateChangeResponse>> =
        runCatching {
            val result = eventApi.getEventStatusHistory(eventId.value)
            val history = result.getOrThrow()
            historyCache?.insertHistory(eventId, history)
            history
        }.recoverCatching {
            historyCache?.getHistoryByEventId(eventId) ?: throw it
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
                eventCache?.insertEvents(result.getOrThrow().map { it.toEvent() })
            }
            events
        }.recoverCatching {
            if (offset == 0) {
                eventCache?.getAllEvents()?.map { it.toListItem() } ?: emptyList()
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
        nomineeId: Id,
    ): Result<Event> {
        val request = InitiateTransferRequest(nomineeId.value)
        return eventApi.initiateTransfer(eventId.value, request).map {
            val updatedEvent = it.toEvent()
            eventCache?.insertEvents(listOf(updatedEvent))
            updatedEvent
        }
    }

    suspend fun respondToTransfer(
        eventId: Id,
        accepted: Boolean,
    ): Result<Event> {
        val request = RespondToTransferRequest(accept = accepted)
        return eventApi.respondToTransfer(eventId.value, request).map {
            val updatedEvent = it.toEvent()
            eventCache?.insertEvents(listOf(updatedEvent))
            updatedEvent
        }
    }
}
