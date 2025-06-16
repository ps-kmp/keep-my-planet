package pt.isel.keepmyplanet.ui.zone.model

sealed class ZoneScreenEvent {
    data class ShowSnackbar(
        val message: String,
    ) : ZoneScreenEvent()

    data object ReportSuccessful : ZoneScreenEvent()
}
