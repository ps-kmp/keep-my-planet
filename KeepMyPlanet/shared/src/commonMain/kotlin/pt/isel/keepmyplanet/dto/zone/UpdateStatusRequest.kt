package pt.isel.keepmyplanet.dto.zone

import kotlinx.serialization.Serializable

@Serializable
data class UpdateStatusRequest(
    val status: String,
)
