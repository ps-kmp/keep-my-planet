package pt.isel.keepmyplanet.domain.src

import kotlin.jvm.JvmInline

@JvmInline
value class Email(
    private val value: String,
) {
    init {
        require(value.isNotBlank()) { "Email cannot be blank" }
        require(value.contains("@")) { "Email should contain '@'" }
    }
}
