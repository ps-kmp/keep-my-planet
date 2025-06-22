package pt.isel.keepmyplanet.ui.zone.report.model

data class ReportZoneUiState(
    val form: ReportZoneFormState = ReportZoneFormState(),
    val actionState: ActionState = ActionState.Idle,
) {
    sealed interface ActionState {
        data object Idle : ActionState

        data object Submitting : ActionState
    }

    val canSubmit: Boolean
        get() = actionState == ActionState.Idle
}
