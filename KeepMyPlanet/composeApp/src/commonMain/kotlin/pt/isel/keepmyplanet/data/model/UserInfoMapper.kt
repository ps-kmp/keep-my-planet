package pt.isel.keepmyplanet.data.model

import pt.isel.keepmyplanet.dto.auth.LoginResponse
import pt.isel.keepmyplanet.dto.user.UserResponse

fun UserResponse.toUserInfo(): UserInfo =
    UserInfo(
        id = id,
        username = name,
        email = email,
        profilePictureId = profilePictureId,
    )

fun LoginResponse.toUserSession(): UserSession =
    UserSession(
        userInfo = user.toUserInfo(),
        token = token,
    )
