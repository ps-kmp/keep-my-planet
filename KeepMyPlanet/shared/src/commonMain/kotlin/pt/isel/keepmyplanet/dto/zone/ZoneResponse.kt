package pt.isel.keepmyplanet.dto.zone

import kotlinx.serialization.Serializable

@Serializable
data class ZoneResponse(
    val id: UInt,
    val latitude: Double,
    val longitude: Double,
    val description: String,
    val reporterId: UInt,
    val eventId: UInt?,
    val status: String,
    val severity: String,
    val photosIds: Set<UInt>,
    val createdAt: String,
    val updatedAt: String,
)
