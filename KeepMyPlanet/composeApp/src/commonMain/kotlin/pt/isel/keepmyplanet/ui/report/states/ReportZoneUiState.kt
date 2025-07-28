package pt.isel.keepmyplanet.ui.report.states

import pt.isel.keepmyplanet.domain.zone.ZoneSeverity
import pt.isel.keepmyplanet.ui.base.UiState

data class ReportZoneUiState(
    val latitude: Double = 0.0,
    val longitude: Double = 0.0,
    val description: String = "",
    val radius: Double = 25.0,
    val severity: ZoneSeverity = ZoneSeverity.LOW,
    val photos: List<SelectedImage> = emptyList(),
    val descriptionError: String? = null,
    val actionState: ActionState = ActionState.Idle,
) : UiState {
    sealed interface ActionState {
        data object Idle : ActionState

        data object Submitting : ActionState
    }

    val canSubmit: Boolean
        get() = actionState == ActionState.Idle

    val hasError: Boolean
        get() = descriptionError != null
}
