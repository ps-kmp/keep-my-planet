package pt.isel.keepmyplanet.dto.user

import kotlinx.serialization.Serializable

@Serializable
data class UpdateProfileRequest(
    val name: String? = null,
    val email: String? = null,
    val profilePictureId: UInt? = null,
)
