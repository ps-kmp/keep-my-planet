package pt.isel.keepmyplanet.ui.zone.update.states

import pt.isel.keepmyplanet.domain.zone.Zone
import pt.isel.keepmyplanet.domain.zone.ZoneSeverity
import pt.isel.keepmyplanet.ui.viewmodel.UiState

data class UpdateZoneUiState(
    val zone: Zone? = null,
    val description: String = "",
    val severity: ZoneSeverity = ZoneSeverity.UNKNOWN,
    val isLoading: Boolean = true,
    val isUpdating: Boolean = false,
    val error: String? = null,
    val descriptionError: String? = null,
) : UiState
