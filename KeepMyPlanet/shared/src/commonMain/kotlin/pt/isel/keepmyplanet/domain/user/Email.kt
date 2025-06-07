package pt.isel.keepmyplanet.domain.user

import kotlinx.serialization.Serializable
import kotlin.jvm.JvmInline

@Serializable
@JvmInline
value class Email(
    val value: String,
) {
    companion object {
        private val EMAIL_REGEX =
            Regex("^[a-zA-Z0-9_+&*-]+(?:\\.[a-zA-Z0-9_+&*-]+)*@(?:[a-zA-Z0-9-]+\\.)+[a-zA-Z]{2,7}$")
    }

    init {
        require(value.isNotBlank()) { "Email cannot be blank." }
        require(EMAIL_REGEX.matches(value)) { "Invalid email format: $value" }
    }
}
