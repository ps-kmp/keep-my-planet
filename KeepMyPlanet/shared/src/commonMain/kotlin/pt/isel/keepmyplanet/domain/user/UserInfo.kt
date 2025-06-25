package pt.isel.keepmyplanet.domain.user

import kotlinx.serialization.Serializable
import pt.isel.keepmyplanet.domain.common.Id

@Serializable
data class UserInfo(
    val id: Id,
    val name: Name,
    val email: Email,
    val profilePictureId: Id?,
)
