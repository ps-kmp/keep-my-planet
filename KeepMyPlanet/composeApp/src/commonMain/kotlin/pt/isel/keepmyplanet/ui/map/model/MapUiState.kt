package pt.isel.keepmyplanet.ui.map.model

import pt.isel.keepmyplanet.dto.zone.ZoneResponse

data class MapUiState(
    val zones: List<ZoneResponse> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null,
)
