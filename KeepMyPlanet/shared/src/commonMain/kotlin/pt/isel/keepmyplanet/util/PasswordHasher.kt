package pt.isel.keepmyplanet.util

import pt.isel.keepmyplanet.domain.user.Password
import pt.isel.keepmyplanet.domain.user.PasswordHash

interface PasswordHasher {
    suspend fun hash(password: Password): PasswordHash

    suspend fun verify(
        password: Password,
        storedHash: PasswordHash,
    ): Boolean
}
