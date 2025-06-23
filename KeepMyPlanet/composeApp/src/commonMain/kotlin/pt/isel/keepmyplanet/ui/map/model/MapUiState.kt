package pt.isel.keepmyplanet.ui.map.model

import pt.isel.keepmyplanet.domain.zone.Zone
import pt.isel.keepmyplanet.ui.base.UiState

data class MapUiState(
    val zones: List<Zone> = emptyList(),
    val isLoading: Boolean = false,
    val isReportingMode: Boolean = false,
    val error: String? = null,
) : UiState
