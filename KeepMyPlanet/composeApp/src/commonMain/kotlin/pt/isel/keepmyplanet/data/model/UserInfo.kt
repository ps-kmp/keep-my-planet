package pt.isel.keepmyplanet.data.model

data class UserInfo(
    val id: UInt,
    val username: String,
    val email: String,
    val profilePictureId: UInt?,
)
