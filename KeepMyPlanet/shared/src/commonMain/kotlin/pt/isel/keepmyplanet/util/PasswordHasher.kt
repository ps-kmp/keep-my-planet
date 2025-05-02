package pt.isel.keepmyplanet.util

import pt.isel.keepmyplanet.domain.user.Password
import pt.isel.keepmyplanet.domain.user.PasswordHash

class PasswordHasher {
    fun hashPassword(password: Password): PasswordHash = PasswordHash("hashed_${password.value}")

    fun verifyPassword(
        password: Password,
        hash: PasswordHash,
    ): Boolean = hash.value == "hashed_${password.value}"
}
