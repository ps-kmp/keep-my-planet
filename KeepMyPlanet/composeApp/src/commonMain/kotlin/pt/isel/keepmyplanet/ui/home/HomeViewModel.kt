package pt.isel.keepmyplanet.ui.home

import kotlinx.coroutines.launch
import pt.isel.keepmyplanet.data.repository.DefaultEventRepository
import pt.isel.keepmyplanet.data.repository.DefaultZoneRepository
import pt.isel.keepmyplanet.domain.event.EventFilterType
import pt.isel.keepmyplanet.domain.event.EventStatus
import pt.isel.keepmyplanet.domain.zone.ZoneStatus
import pt.isel.keepmyplanet.session.SessionManager
import pt.isel.keepmyplanet.ui.base.BaseViewModel
import pt.isel.keepmyplanet.ui.home.states.HomeEvent
import pt.isel.keepmyplanet.ui.home.states.HomeUiState
import pt.isel.keepmyplanet.utils.now

class HomeViewModel(
    private val eventRepository: DefaultEventRepository,
    private val zoneRepository: DefaultZoneRepository,
    private val sessionManager: SessionManager,
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
        if (currentState.nearbyZones.isNotEmpty() || currentState.isLoading) return
        loadNearbyZones(latitude, longitude)
    }

    private fun loadUpcomingEvents() {
        viewModelScope.launch {
            setState { copy(isLoading = true) }
            try {
                val organizedResult =
                    eventRepository.searchEvents(EventFilterType.ORGANIZED, null, 5, 0)
                val joinedResult = eventRepository.searchEvents(EventFilterType.JOINED, null, 5, 0)

                val upcomingEvents =
                    (organizedResult.getOrNull().orEmpty() + joinedResult.getOrNull().orEmpty())
                        .distinctBy { it.id }
                        .filter { it.status == EventStatus.PLANNED && it.period.start > now() }
                        .sortedBy { it.period.start }
                        .take(5)

                setState { copy(upcomingEvents = upcomingEvents) }
            } finally {
                setState { copy(isLoading = false) }
            }
        }
    }

    private fun loadNearbyZones(
        latitude: Double,
        longitude: Double,
    ) {
        viewModelScope.launch {
            setState { copy(isLoading = true, error = null) }
            zoneRepository
                .findZonesByLocation(latitude, longitude, radius = 10000.0)
                .onSuccess { zones ->
                    val reportedZones =
                        zones
                            .filter { it.status == ZoneStatus.REPORTED }
                            .sortedBy { it.zoneSeverity.ordinal }
                            .take(5)
                    setState { copy(nearbyZones = reportedZones, isLoading = false) }
                }.onFailure {
                    setState {
                        copy(
                            isLoading = false,
                            error = getErrorMessage("Could not load nearby zones", it),
                        )
                    }
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
