package pt.isel.keepmyplanet.dto.auth

import kotlinx.serialization.Serializable
import pt.isel.keepmyplanet.dto.user.UserResponse

@Serializable
data class LoginResponse(
    val token: String,
    val user: UserResponse,
)
