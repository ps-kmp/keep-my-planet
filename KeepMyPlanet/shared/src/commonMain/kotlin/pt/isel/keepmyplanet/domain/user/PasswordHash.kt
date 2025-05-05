package pt.isel.keepmyplanet.domain.user

import kotlin.jvm.JvmInline

@JvmInline
value class PasswordHash(
    val value: String,
) {
    init {
        require(value.isNotBlank()) { "Password hash cannot be blank." }
    }
}
