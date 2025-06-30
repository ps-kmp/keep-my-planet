package pt.isel.keepmyplanet.domain.user

import pt.isel.keepmyplanet.domain.common.Id

data class UserCacheInfo(
    val id: Id,
    val name: Name,
    val email: Email,
    val profilePictureUrl: String?,
)
