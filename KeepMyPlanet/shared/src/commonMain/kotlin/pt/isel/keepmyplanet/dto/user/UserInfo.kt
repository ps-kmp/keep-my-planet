package pt.isel.keepmyplanet.dto.user

import kotlinx.serialization.Serializable
import pt.isel.keepmyplanet.domain.common.Id
import pt.isel.keepmyplanet.domain.user.Email
import pt.isel.keepmyplanet.domain.user.Name

@Serializable
data class UserInfo(
    val id: Id,
    val name: Name,
    val email: Email,
    val profilePictureId: Id?,
)
