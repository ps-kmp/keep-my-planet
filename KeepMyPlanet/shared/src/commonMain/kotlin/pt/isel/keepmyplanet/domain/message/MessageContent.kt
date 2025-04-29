package pt.isel.keepmyplanet.domain.message

import kotlin.jvm.JvmInline

@JvmInline
value class MessageContent(
    val value: String,
) {
    init {
        require(value.isNotBlank()) { "Message content cannot be blank." }
        require(value.length <= 1000) { "Message content cannot exceed 1000 characters." }
    }
}
