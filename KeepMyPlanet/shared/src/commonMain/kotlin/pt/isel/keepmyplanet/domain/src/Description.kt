package pt.isel.keepmyplanet.domain.src

import kotlin.jvm.JvmInline

@JvmInline
value class Description(
    private val text: String,
) {
    init {
        require(text.isNotBlank()) { "Description cannot be blank" }
    }
}
