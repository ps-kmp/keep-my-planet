package pt.isel.keepmyplanet.ui.report.states

import pt.isel.keepmyplanet.ui.base.UiEvent

sealed interface ReportZoneEvent : UiEvent {
    data class ShowSnackbar(
        val message: String,
    ) : ReportZoneEvent

    data object ReportSuccessful : ReportZoneEvent
}
