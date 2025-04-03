package pt.isel.keepmyplanet.repository

import pt.isel.keepmyplanet.domain.user.Email
import pt.isel.keepmyplanet.domain.user.Name
import pt.isel.keepmyplanet.domain.user.User

interface AuthRepository {
    suspend fun authenticate(
        email: Email,
        password: String,
    ): User?

    suspend fun register(
        name: Name,
        email: Email,
        password: String,
    ): User

    suspend fun userExistsByEmail(email: Email): Boolean
}
