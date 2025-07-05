package pt.isel.keepmyplanet.dto.zone

import kotlinx.serialization.Serializable

@Serializable
data class AddPhotoRequest(
    val photoId: UInt,
    val type: String,
)
