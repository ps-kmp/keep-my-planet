package pt.isel.keepmyplanet.domain.common

import kotlinx.serialization.Serializable

@Serializable
data class Place(
    val displayName: String,
    val latitude: Double,
    val longitude: Double,
)
