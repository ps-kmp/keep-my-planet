package pt.isel.keepmyplanet.domain.src

import kotlin.jvm.JvmInline

@JvmInline
value class Name(
    private val value: String,
) {
    init {
        require(value.isNotBlank()) { "Name cannot be blank" }
    }
}
