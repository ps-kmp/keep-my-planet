package pt.isel.keepmyplanet.data.mapper

import pt.isel.keepmyplanet.domain.common.Id
import pt.isel.keepmyplanet.domain.user.Email
import pt.isel.keepmyplanet.domain.user.Name
import pt.isel.keepmyplanet.dto.user.UserResponse
import pt.isel.keepmyplanet.ui.user.model.UserInfo

fun UserResponse.toUserInfo(): UserInfo =
    UserInfo(
        id = Id(id),
        name = Name(name),
        email = Email(email),
        profilePictureId = profilePictureId?.let { Id(it) },
    )
