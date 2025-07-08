package pt.isel.keepmyplanet.domain.user

import kotlinx.datetime.LocalDateTime
import pt.isel.keepmyplanet.domain.common.Id

data class User(
    val id: Id,
    val name: Name,
    val email: Email,
    val passwordHash: PasswordHash,
    val role: UserRole = UserRole.USER,
    val profilePictureId: Id? = null,
    val createdAt: LocalDateTime,
    val updatedAt: LocalDateTime,
)
