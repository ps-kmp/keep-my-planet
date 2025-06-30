package pt.isel.keepmyplanet.data.repository

import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import pt.isel.keepmyplanet.data.api.EventApi
import pt.isel.keepmyplanet.domain.common.Id
import pt.isel.keepmyplanet.domain.event.Event
import pt.isel.keepmyplanet.domain.user.UserInfo
import pt.isel.keepmyplanet.mapper.event.toEvent
import pt.isel.keepmyplanet.mapper.user.toUserInfo

data class EventDetailsBundle(
    val event: Event,
    val participants: List<UserInfo>,
)

class EventsRepository(
    private val eventApi: EventApi,
    private val eventCache: EventCacheRepository,
    private val userCache: UserCacheRepository,
) {
    suspend fun getEventDetailsBundle(eventId: Id): Result<EventDetailsBundle> {
        val networkResult =
            runCatching {
                coroutineScope {
                    val detailsDeferred = async { eventApi.getEventDetails(eventId.value) }
                    val participantsDeferred =
                        async { eventApi.getEventParticipants(eventId.value) }

                    val event = detailsDeferred.await().getOrThrow().toEvent()
                    val participants =
                        participantsDeferred.await().getOrThrow().map {
                            it
                                .toUserInfo()
                        }

                    eventCache.insertEvents(listOf(event))
                    userCache.insertUsers(participants.map { it.toUserCacheInfo() })

                    EventDetailsBundle(event, participants)
                }
            }

        if (networkResult.isSuccess) return networkResult

        val cachedEvent = eventCache.getEventById(eventId)
        if (cachedEvent != null) {
            val cachedParticipants = userCache.getUsersByIds(cachedEvent.participantsIds.toList())
            if (cachedParticipants.size == cachedEvent.participantsIds.size) {
                return Result.success(
                    EventDetailsBundle(cachedEvent, cachedParticipants.map { it.toUserInfo() }),
                )
            }
        }

        return networkResult
    }

    suspend fun getEventDetails(id: Id): Result<Event> {
        val networkResult = eventApi.getEventDetails(id.value).map { it.toEvent() }

        return if (networkResult.isSuccess) {
            networkResult.onSuccess { eventCache.insertEvents(listOf(it)) }
            networkResult
        } else {
            val cached = eventCache.getEventById(id)
            cached?.let { Result.success(it) } ?: networkResult
        }
    }
}
