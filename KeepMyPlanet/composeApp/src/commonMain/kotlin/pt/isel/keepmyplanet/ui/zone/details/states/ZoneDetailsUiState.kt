package pt.isel.keepmyplanet.ui.zone.details.states

import pt.isel.keepmyplanet.domain.zone.Zone
import pt.isel.keepmyplanet.ui.viewmodel.UiState

data class ZoneDetailsUiState(
    val zone: Zone? = null,
    val isLoading: Boolean = false,
    val photoUrls: List<String> = emptyList(),
    val error: String? = null,
    val canUserManageZone: Boolean = false,
    val actionState: ActionState = ActionState.IDLE,
) : UiState {
    sealed interface ActionState {
        data object IDLE : ActionState

        data object DELETING : ActionState
    }

    val isActionInProgress: Boolean
        get() = actionState != ActionState.IDLE
}
