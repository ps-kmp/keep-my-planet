package pt.isel.keepmyplanet.ui.zone.report.model

sealed interface ReportZoneEvent {
    data class ShowSnackbar(
        val message: String,
    ) : ReportZoneEvent

    data object ReportSuccessful : ReportZoneEvent
}
