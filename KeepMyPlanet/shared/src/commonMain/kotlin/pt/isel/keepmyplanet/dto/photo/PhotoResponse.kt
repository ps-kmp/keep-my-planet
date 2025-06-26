package pt.isel.keepmyplanet.dto.photo

import kotlinx.serialization.Serializable

@Serializable
data class PhotoResponse(
    val id: UInt,
    val url: String,
    val uploaderId: UInt,
    val uploadedAt: String,
)
