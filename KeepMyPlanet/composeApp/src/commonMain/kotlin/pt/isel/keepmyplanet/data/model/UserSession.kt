package pt.isel.keepmyplanet.data.model

import kotlinx.serialization.Serializable

@Serializable
data class UserSession(
    val userId: UInt,
    val username: String,
)
