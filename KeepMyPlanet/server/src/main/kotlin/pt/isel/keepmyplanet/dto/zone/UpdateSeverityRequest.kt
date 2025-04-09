package pt.isel.keepmyplanet.dto.zone

import kotlinx.serialization.Serializable

@Serializable
data class UpdateSeverityRequest(
    val severity: String,
)
