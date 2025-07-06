package pt.isel.keepmyplanet.dto.zone

import kotlinx.serialization.Serializable

@Serializable
data class UpdateZoneRequest(
    val radius: Double? = null,
    val description: String? = null,
    val status: String? = null,
    val severity: String? = null,
)
