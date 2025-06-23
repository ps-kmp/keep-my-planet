package pt.isel.keepmyplanet.ui.zone.details.model

import pt.isel.keepmyplanet.domain.zone.Zone
import pt.isel.keepmyplanet.ui.base.UiState

data class ZoneDetailsUiState(
    val zone: Zone? = null,
    val isLoading: Boolean = false,
    val error: String? = null,
) : UiState
