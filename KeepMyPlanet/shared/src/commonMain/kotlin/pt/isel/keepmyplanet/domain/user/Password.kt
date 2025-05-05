package pt.isel.keepmyplanet.domain.user

import kotlin.jvm.JvmInline

@JvmInline
value class Password(
    val value: String,
) {
    companion object {
        const val MIN_LENGTH = 8
    }

    init {
        require(value.isNotBlank()) {
            "Password cannot be blank."
        }
        require(value.length >= MIN_LENGTH) {
            "Password must be at least $MIN_LENGTH characters long."
        }
        require(value.any { it.isDigit() }) {
            "Password must contain at least one digit."
        }
        require(value.any { it.isUpperCase() }) {
            "Password must contain at least one uppercase letter."
        }
        require(value.any { it.isLowerCase() }) {
            "Password must contain at least one lowercase letter."
        }
        require(value.any { !it.isLetterOrDigit() }) {
            "Password must contain at least one special character."
        }
    }
}
