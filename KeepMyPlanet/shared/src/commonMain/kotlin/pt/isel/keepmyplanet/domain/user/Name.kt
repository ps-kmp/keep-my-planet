package pt.isel.keepmyplanet.domain.user

import kotlin.jvm.JvmInline
import kotlinx.serialization.Serializable

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
