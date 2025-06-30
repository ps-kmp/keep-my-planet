package pt.isel.keepmyplanet.mapper.user

import pt.isel.keepmyplanet.domain.common.Id
import pt.isel.keepmyplanet.domain.user.Email
import pt.isel.keepmyplanet.domain.user.Name
import pt.isel.keepmyplanet.domain.user.UserCacheInfo
import pt.isel.keepmyplanet.domain.user.UserInfo
import pt.isel.keepmyplanet.dto.user.UserResponse

fun UserResponse.toUserInfo(): UserInfo =
    UserInfo(
        id = Id(id),
        name = Name(name),
        email = Email(email),
        profilePictureId = profilePictureId?.let { Id(it) },
    )

fun UserCacheInfo.toUserInfo(): UserInfo =
    UserInfo(
        id = this.id,
        name = this.name,
        email = this.email,
        profilePictureId = null,
    )
