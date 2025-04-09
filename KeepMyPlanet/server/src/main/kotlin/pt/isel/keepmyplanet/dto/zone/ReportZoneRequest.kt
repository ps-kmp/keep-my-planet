package pt.isel.keepmyplanet.dto.zone

import kotlinx.serialization.Serializable

@Serializable
data class ReportZoneRequest(
    val latitude: Double,
    val longitude: Double,
    val description: String,
    val photoIds: Set<UInt>,
    val severity: String? = null,
)
