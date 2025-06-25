package pt.isel.keepmyplanet.ui.report.states

import pt.isel.keepmyplanet.domain.zone.ZoneSeverity

data class ReportZoneFormState(
    val latitude: Double = 0.0,
    val longitude: Double = 0.0,
    val description: String = "",
    val severity: ZoneSeverity = ZoneSeverity.LOW,
    val descriptionError: String? = null,
) {
    val hasError: Boolean
        get() = descriptionError != null
}
