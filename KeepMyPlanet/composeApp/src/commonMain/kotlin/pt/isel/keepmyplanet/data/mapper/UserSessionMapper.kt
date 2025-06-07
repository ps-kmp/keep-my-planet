package pt.isel.keepmyplanet.data.mapper

import pt.isel.keepmyplanet.dto.auth.LoginResponse
import pt.isel.keepmyplanet.session.model.UserSession

fun LoginResponse.toUserSession(): UserSession =
    UserSession(
        userInfo = user.toUserInfo(),
        token = token,
    )
