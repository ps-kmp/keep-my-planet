package pt.isel.keepmyplanet.ui.home

import com.russhwolf.settings.Settings
import com.russhwolf.settings.set
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
import pt.isel.keepmyplanet.data.cache.EventCacheRepository
import pt.isel.keepmyplanet.data.repository.EventApiRepository
import pt.isel.keepmyplanet.data.repository.ZoneApiRepository
import pt.isel.keepmyplanet.domain.event.EventFilterType
import pt.isel.keepmyplanet.domain.event.EventStatus
import pt.isel.keepmyplanet.domain.user.UserRole
import pt.isel.keepmyplanet.domain.zone.ZoneStatus
import pt.isel.keepmyplanet.mapper.event.toListItem
import pt.isel.keepmyplanet.session.SessionManager
import pt.isel.keepmyplanet.ui.base.BaseViewModel
import pt.isel.keepmyplanet.ui.home.states.HomeEvent
import pt.isel.keepmyplanet.ui.home.states.HomeUiState
import pt.isel.keepmyplanet.utils.now

private const val ONBOARDING_COMPLETED_KEY = "onboarding_completed"

class HomeViewModel(
    private val eventRepository: EventApiRepository,
    private val zoneRepository: ZoneApiRepository,
    private val sessionManager: SessionManager,
    private val settings: Settings,
    private val eventCache: EventCacheRepository?,
) : BaseViewModel<HomeUiState>(HomeUiState(), sessionManager) {
    init {
        viewModelScope.launch {
            val onboardingCompleted = settings.getBoolean(ONBOARDING_COMPLETED_KEY, false)
            val user = sessionManager.userSession.value?.userInfo

            if (!onboardingCompleted) {
                setState { copy(showOnboarding = true) }
            }

            if (user == null) {
                setState { copy(isLoading = false, user = null, isUserAdmin = false) }
            } else {
                setState { copy(user = user, isUserAdmin = user.role == UserRole.ADMIN) }
                loadDashboardData()
                onFindNearbyZonesRequested()
            }
        }
    }

    fun completeOnboarding() {
        settings[ONBOARDING_COMPLETED_KEY] = true
        setState { copy(showOnboarding = false) }
    }

    override fun handleErrorWithMessage(message: String) {
        sendEvent(HomeEvent.ShowSnackbar(message))
    }

    fun onFindNearbyZonesRequested() {
        setState { copy(isFindingZones = true, zonesFound = null, nearbyZones = emptyList()) }
        sendEvent(HomeEvent.RequestLocation)
    }

    fun onLocationAvailable(
        latitude: Double,
        longitude: Double,
    ) {
        viewModelScope.launch {
            zoneRepository
                .findZonesByLocation(latitude, longitude, radius = 10000.0)
                .onSuccess { zones ->
                    val reportedZones =
                        zones
                            .filter { it.status == ZoneStatus.REPORTED }
                            .sortedBy { it.zoneSeverity.ordinal }
                            .take(5)
                    setState {
                        copy(
                            nearbyZones = reportedZones,
                            isFindingZones = false,
                            zonesFound = reportedZones.isNotEmpty(),
                        )
                    }
                }.onFailure {
                    setState { copy(isFindingZones = false, zonesFound = false) }
                    handleErrorWithMessage(getErrorMessage("Could not load nearby zones", it))
                }
        }
    }

    fun onLocationError() {
        setState { copy(isFindingZones = false, zonesFound = false) }
        handleErrorWithMessage(
            "Unable to retrieve your location. Please check permissions and try again.",
        )
    }

    private fun loadDashboardData() {
        viewModelScope.launch {
            val user = currentState.user ?: return@launch
            setState { copy(isLoading = true) }

            val cachedEvents =
                eventCache?.getAllEvents()?.filter {
                    it.organizerId == user.id || user.id in it.participantsIds
                }

            cachedEvents?.let { events ->
                val upcoming =
                    events
                        .filter { event ->
                            event.status == EventStatus.PLANNED &&
                                event.period.start > now()
                        }.sortedBy { event -> event.period.start }
                        .take(5)
                        .map { event -> event.toListItem() }

                val pending = events.filter { event -> event.pendingOrganizerId == user.id }

                setState {
                    copy(upcomingEvents = upcoming, pendingActions = pending)
                }
            }

            try {
                coroutineScope {
                    val organizedDeferred =
                        async {
                            eventRepository.searchFullEvents(
                                EventFilterType.ORGANIZED,
                                null,
                                20,
                                0,
                            )
                        }
                    val joinedDeferred =
                        async {
                            eventRepository.searchFullEvents(
                                EventFilterType.JOINED,
                                null,
                                20,
                                0,
                            )
                        }

                    val allUserEvents =
                        (
                            organizedDeferred.await().getOrNull().orEmpty() +
                                joinedDeferred.await().getOrNull().orEmpty()
                        ).distinctBy { it.id }

                    val networkUpcoming =
                        allUserEvents
                            .filter { it.status == EventStatus.PLANNED && it.period.start > now() }
                            .sortedBy { it.period.start }
                            .take(5)
                            .map { it.toListItem() }

                    val networkPending =
                        allUserEvents
                            .filter { it.pendingOrganizerId == user.id }

                    setState {
                        copy(
                            upcomingEvents = networkUpcoming,
                            pendingActions = networkPending,
                        )
                    }
                }
            } catch (e: Exception) {
                handleErrorWithMessage(getErrorMessage("Failed to refresh dashboard data", e))
            } finally {
                setState { copy(isLoading = false) }
            }
        }
    }
}
