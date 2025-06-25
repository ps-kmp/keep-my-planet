package pt.isel.keepmyplanet.mapper.user

import pt.isel.keepmyplanet.domain.user.UserSession
import pt.isel.keepmyplanet.dto.auth.LoginResponse

fun LoginResponse.toUserSession(): UserSession =
    UserSession(
        userInfo = user.toUserInfo(),
        token = token,
    )
