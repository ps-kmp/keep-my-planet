package pt.isel.keepmyplanet.ui.report.states

import pt.isel.keepmyplanet.ui.viewmodel.UiState

data class ReportZoneUiState(
    val form: ReportZoneFormState = ReportZoneFormState(),
    val actionState: ActionState = ActionState.Idle,
) : UiState {
    sealed interface ActionState {
        data object Idle : ActionState

        data object Submitting : ActionState
    }

    val canSubmit: Boolean
        get() = actionState == ActionState.Idle
}
