package pt.isel.keepmyplanet.ui.zone.model

import pt.isel.keepmyplanet.domain.zone.Zone

data class ZoneUiState(
    val zoneDetails: Zone? = null,
    val isLoading: Boolean = false,
    val reportForm: ReportZoneFormState = ReportZoneFormState(),
)
