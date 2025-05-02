package pt.isel.keepmyplanet.mapper.user

import pt.isel.keepmyplanet.domain.user.User
import pt.isel.keepmyplanet.dto.user.UserResponse

fun User.toResponse(): UserResponse =
    UserResponse(
        id = id.value,
        name = name.value,
        email = email.value,
        profilePictureId = profilePictureId?.value,
        createdAt = createdAt.toString(),
        updatedAt = updatedAt.toString(),
    )
