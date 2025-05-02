package pt.isel.keepmyplanet.dto.user

import kotlinx.serialization.Serializable

@Serializable
data class UserResponse(
    val id: UInt,
    val name: String,
    val email: String,
    val profilePictureId: UInt?,
    val createdAt: String,
    val updatedAt: String,
)
