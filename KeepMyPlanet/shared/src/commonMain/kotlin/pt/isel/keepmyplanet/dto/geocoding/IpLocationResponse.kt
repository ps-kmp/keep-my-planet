package pt.isel.keepmyplanet.dto.geocoding

import kotlinx.serialization.Serializable

@Serializable
data class IpLocationResponse(
    val loc: String,
)
