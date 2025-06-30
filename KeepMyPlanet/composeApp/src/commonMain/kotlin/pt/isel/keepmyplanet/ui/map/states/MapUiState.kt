package pt.isel.keepmyplanet.ui.map.states

import pt.isel.keepmyplanet.domain.common.Place
import pt.isel.keepmyplanet.domain.zone.Zone
import pt.isel.keepmyplanet.ui.base.UiState

data class MapUiState(
    val zones: List<Zone> = emptyList(),
    val isLoading: Boolean = false,
    val isReportingMode: Boolean = false,
    val error: String? = null,
    val isLocatingUser: Boolean = false,
    val selectedZoneId: String? = null,
    val searchQuery: String = "",
    val searchResults: List<Place> = emptyList(),
    val isSearching: Boolean = false,
) : UiState
