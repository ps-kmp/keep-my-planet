package pt.isel.keepmyplanet.ui.zone.model

import pt.isel.keepmyplanet.domain.zone.ZoneSeverity

data class ReportZoneFormState(
    val latitude: Double = 0.0,
    val longitude: Double = 0.0,
    val description: String = "",
    val severity: ZoneSeverity = ZoneSeverity.LOW,
    val isSubmitting: Boolean = false,
) {
    val canSubmit: Boolean
        get() = description.isNotBlank() && !isSubmitting
}
