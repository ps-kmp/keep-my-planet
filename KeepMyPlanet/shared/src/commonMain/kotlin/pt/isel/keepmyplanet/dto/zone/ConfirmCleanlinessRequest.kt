package pt.isel.keepmyplanet.dto.zone

import kotlinx.serialization.Serializable

@Serializable
data class ConfirmCleanlinessRequest(
    val wasCleaned: Boolean,
    val eventId: UInt,
    val newSeverity: String? = null,
)
