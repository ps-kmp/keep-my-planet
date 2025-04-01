package pt.isel.keepmyplanet.domain.common

import kotlin.jvm.JvmInline

@JvmInline
value class Description(val value: String) {
    init {
        require(value.isNotBlank()) { "Description cannot be blank" }
        require(value.length <= 1000) { "Description cannot exceed 1000 characters." }
    }
}
