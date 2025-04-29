package pt.isel.keepmyplanet.data.model

data class UserSession(
    val userInfo: UserInfo,
    val token: String,
)
