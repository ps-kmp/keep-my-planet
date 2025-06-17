package pt.isel.keepmyplanet.ui.zone.report.model

sealed class ReportZoneScreenEvent {
    data class ShowSnackbar(
        val message: String,
    ) : ReportZoneScreenEvent()

    data object ReportSuccessful : ReportZoneScreenEvent()
}
