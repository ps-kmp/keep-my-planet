package pt.isel.keepmyplanet.ui.home.states

import pt.isel.keepmyplanet.domain.event.EventListItem
import pt.isel.keepmyplanet.domain.user.UserInfo
import pt.isel.keepmyplanet.domain.zone.Zone
import pt.isel.keepmyplanet.ui.base.UiState

data class HomeUiState(
    val user: UserInfo? = null,
    val isLoading: Boolean = true,
    val isLoadingZones: Boolean = false,
    val isLocating: Boolean = false,
    val upcomingEvents: List<EventListItem> = emptyList(),
    val nearbyZones: List<Zone> = emptyList(),
    val error: String? = null,
    val locationError: Boolean = false,
    val isUserAdmin: Boolean = false,
    val showOnboarding: Boolean = false,
) : UiState
