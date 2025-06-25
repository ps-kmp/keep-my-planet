package pt.isel.keepmyplanet.domain.user

import kotlinx.serialization.Serializable

@Serializable
data class UserSession(
    val userInfo: UserInfo,
    val token: String,
)
