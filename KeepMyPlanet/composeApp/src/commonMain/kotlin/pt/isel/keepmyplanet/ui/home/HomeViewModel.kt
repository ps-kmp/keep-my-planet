package pt.isel.keepmyplanet.ui.home

import kotlinx.coroutines.launch
import pt.isel.keepmyplanet.data.cache.EventCacheRepository
import pt.isel.keepmyplanet.data.cache.ZoneCacheRepository
import pt.isel.keepmyplanet.data.repository.DefaultEventRepository
import pt.isel.keepmyplanet.data.repository.DefaultZoneRepository
import pt.isel.keepmyplanet.domain.event.EventFilterType
import pt.isel.keepmyplanet.domain.event.EventStatus
import pt.isel.keepmyplanet.domain.zone.ZoneStatus
import pt.isel.keepmyplanet.mapper.event.toListItem
import pt.isel.keepmyplanet.session.SessionManager
import pt.isel.keepmyplanet.ui.base.BaseViewModel
import pt.isel.keepmyplanet.ui.home.states.HomeEvent
import pt.isel.keepmyplanet.ui.home.states.HomeUiState
import pt.isel.keepmyplanet.utils.now

class HomeViewModel(
    private val eventRepository: DefaultEventRepository,
    private val zoneRepository: DefaultZoneRepository,
    private val sessionManager: SessionManager,
    private val eventCache: EventCacheRepository?,
    private val zoneCache: ZoneCacheRepository?,
) : BaseViewModel<HomeUiState>(HomeUiState()) {
    init {
        val user = sessionManager.userSession.value?.userInfo
        if (user == null) {
            setState { copy(error = "User not logged in.") }
        } else {
            setState { copy(user = user) }
            loadUpcomingEvents()
        }
    }

    override fun handleErrorWithMessage(message: String) {
        sendEvent(HomeEvent.ShowSnackbar(message))
    }

    fun onLocationAvailable(
        latitude: Double,
        longitude: Double,
    ) {
        viewModelScope.launch {
            val cachedZones =
                zoneCache
                    ?.getZonesInBoundingBox(
                        min =
                            pt.isel.keepmyplanet.domain.zone
                                .Location(latitude - 0.1, longitude - 0.1),
                        max =
                            pt.isel.keepmyplanet.domain.zone
                                .Location(latitude + 0.1, longitude + 0.1),
                    )?.filter { it.status == ZoneStatus.REPORTED }
                    ?.sortedBy { it.zoneSeverity.ordinal }
                    ?.take(5)

            if (cachedZones?.isNotEmpty() == true) {
                setState { copy(nearbyZones = cachedZones) }
            } else {
                setState { copy(isLoadingZones = true) }
            }

            zoneRepository
                .findZonesByLocation(latitude, longitude, radius = 10000.0)
                .onSuccess { zones ->
                    val reportedZones =
                        zones
                            .filter { it.status == ZoneStatus.REPORTED }
                            .sortedBy { it.zoneSeverity.ordinal }
                            .take(5)
                    setState { copy(nearbyZones = reportedZones, isLoadingZones = false) }
                }.onFailure {
                    setState { copy(isLoadingZones = false) }
                    if (currentState.nearbyZones.isEmpty()) {
                        handleErrorWithMessage(getErrorMessage("Could not load nearby zones", it))
                    }
                }
        }
    }

    private fun loadUpcomingEvents() {
        viewModelScope.launch {
            val user = currentState.user ?: return@launch

            val cachedEvents =
                eventCache
                    ?.getAllEvents()
                    ?.filter { it.organizerId == user.id || user.id in it.participantsIds }
                    ?.distinctBy { it.id }
                    ?.filter { it.status == EventStatus.PLANNED && it.period.start > now() }
                    ?.sortedBy { it.period.start }
                    ?.take(5)
                    ?.map { it.toListItem() }

            if (cachedEvents?.isNotEmpty() == true) {
                setState { copy(upcomingEvents = cachedEvents, isLoading = false) }
            } else {
                setState { copy(isLoading = true) }
            }

            try {
                val organizedResult =
                    eventRepository.searchEvents(EventFilterType.ORGANIZED, null, 5, 0)
                val joinedResult = eventRepository.searchEvents(EventFilterType.JOINED, null, 5, 0)

                val networkEvents =
                    (organizedResult.getOrNull().orEmpty() + joinedResult.getOrNull().orEmpty())
                        .distinctBy { it.id }
                        .filter { it.status == EventStatus.PLANNED && it.period.start > now() }
                        .sortedBy { it.period.start }
                        .take(5)

                setState { copy(upcomingEvents = networkEvents) }
            } finally {
                setState { copy(isLoading = false) }
            }
        }
    }

    fun requestLocationUpdate() {
        if (!currentState.isLocating) {
            setState { copy(isLocating = true, locationError = false) }
            sendEvent(HomeEvent.RequestLocation)
        }
    }

    fun onLocationUpdateReceived() {
        setState { copy(isLocating = false) }
    }

    fun onLocationError() {
        setState { copy(isLocating = false, locationError = true) }
        handleErrorWithMessage("Unable to retrieve your location.")
    }
}
