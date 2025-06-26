package pt.isel.keepmyplanet.ui.zone.states

import pt.isel.keepmyplanet.domain.zone.Zone
import pt.isel.keepmyplanet.ui.viewmodel.UiState

data class ZoneDetailsUiState(
    val zone: Zone? = null,
    val isLoading: Boolean = false,
    val photoUrls: List<String> = emptyList(),
    val error: String? = null,
) : UiState
