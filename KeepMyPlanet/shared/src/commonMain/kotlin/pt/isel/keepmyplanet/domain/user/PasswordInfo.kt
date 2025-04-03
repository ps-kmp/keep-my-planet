package pt.isel.keepmyplanet.domain.user

import kotlin.jvm.JvmInline

@JvmInline
value class PasswordInfo(
    val value: String,
) {
    init {
        require(value.length >= 8) { "Password must be at least 8 characters long" }
        require(value.any { it.isDigit() }) { "Password must contain at least one digit" }
        require(value.any { it.isUpperCase() }) {
            "Password must contain at least one uppercase letter"
        }
        require(value.any { it.isLowerCase() }) {
            "Password must contain at least one lowercase letter"
        }
    }
}
