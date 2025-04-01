package pt.isel.keepmyplanet.domain.common

import kotlin.jvm.JvmInline

@JvmInline
value class Title(val value: String) {
    init {
        require(value.isNotBlank()) { "Title cannot be blank" }
        require(value.length <= 150) { "Title cannot exceed 150 characters." }
    }
}
