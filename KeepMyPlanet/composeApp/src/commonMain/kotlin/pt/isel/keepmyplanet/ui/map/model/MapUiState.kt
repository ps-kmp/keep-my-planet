package pt.isel.keepmyplanet.ui.map.model

import pt.isel.keepmyplanet.domain.zone.Zone

data class MapUiState(
    val zones: List<Zone> = emptyList(),
    val isLoading: Boolean = false,
    val isReportingMode: Boolean = false,
)
