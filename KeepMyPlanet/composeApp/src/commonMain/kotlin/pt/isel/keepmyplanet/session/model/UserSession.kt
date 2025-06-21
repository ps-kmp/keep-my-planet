package pt.isel.keepmyplanet.session.model

import kotlinx.serialization.Serializable
import pt.isel.keepmyplanet.ui.user.profile.model.UserInfo

@Serializable
data class UserSession(
    val userInfo: UserInfo,
    val token: String,
)
