package pt.isel.keepmyplanet.data.cache.models

import pt.isel.keepmyplanet.domain.zone.ZoneSeverity
import pt.isel.keepmyplanet.ui.report.states.SelectedImage

data class QueuedReport(
    val id: Long,
    val latitude: Double,
    val longitude: Double,
    val radius: Double,
    val description: String,
    val severity: ZoneSeverity,
    val photos: List<SelectedImage>,
    val retries: Long,
)
