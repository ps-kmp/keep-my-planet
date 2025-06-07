package pt.isel.keepmyplanet.domain.user

import kotlinx.serialization.Serializable
import kotlin.jvm.JvmInline

@Serializable
@JvmInline
value class Name(
    val value: String,
) {
    init {
        require(value.isNotBlank()) { "Name cannot be blank" }
        require(value.length <= 30) { "Name cannot exceed 30 characters." }
    }
}
