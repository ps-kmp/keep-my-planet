package pt.isel.keepmyplanet.mapper.user

import pt.isel.keepmyplanet.domain.user.UserCacheInfo
import pt.isel.keepmyplanet.domain.user.UserInfo

fun UserInfo.toUserCacheInfo(): UserCacheInfo =
    UserCacheInfo(
        id = this.id,
        name = this.name,
        email = this.email,
        profilePictureUrl = null,
    )
