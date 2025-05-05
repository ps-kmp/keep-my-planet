package pt.isel.keepmyplanet.domain.common

import kotlin.jvm.JvmInline

@JvmInline
value class Url(
    val value: String,
) {
    init {
        require(value.isNotBlank()) { "URL string cannot be blank." }
        require(!value.contains(Regex("\\s"))) {
            "URL string cannot contain whitespace. Provided: '$value'"
        }
        val schemeSeparatorIndex = value.indexOf("://")
        require(schemeSeparatorIndex > 0) {
            "URL string must contain a valid scheme separator '://'. Provided: '$value'"
        }
        val scheme = value.substring(0, schemeSeparatorIndex).lowercase()
        require(scheme == "http" || scheme == "https") {
            "URL scheme must be http or https. Found scheme: '$scheme' in URL: '$value'"
        }
    }
}
