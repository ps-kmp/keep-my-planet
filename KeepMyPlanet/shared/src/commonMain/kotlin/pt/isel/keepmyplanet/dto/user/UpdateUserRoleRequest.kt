package pt.isel.keepmyplanet.dto.user

import kotlinx.serialization.Serializable
import pt.isel.keepmyplanet.domain.user.UserRole

@Serializable
data class UpdateUserRoleRequest(
    val role: UserRole,
)
