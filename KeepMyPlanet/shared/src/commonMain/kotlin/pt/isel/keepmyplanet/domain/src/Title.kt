package pt.isel.keepmyplanet.domain.src

import kotlin.jvm.JvmInline

@JvmInline
value class Title(
    private val value: String,
) {
    init {
        require(value.isNotBlank()) { "Title cannot be blank" }
    }
}
