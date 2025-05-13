package pt.isel.keepmyplanet.data.model

import pt.isel.keepmyplanet.dto.user.UserResponse

fun UserResponse.toUserInfo(): UserInfo =
    UserInfo(
        id = id,
        username = name,
        email = email,
        profilePictureId = profilePictureId,
    )
