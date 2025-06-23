package pt.isel.keepmyplanet.mapper.user

import pt.isel.keepmyplanet.dto.auth.LoginResponse
import pt.isel.keepmyplanet.dto.user.UserSession

fun LoginResponse.toUserSession(): UserSession =
    UserSession(
        userInfo = user.toUserInfo(),
        token = token,
    )
