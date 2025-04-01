package pt.isel.keepmyplanet.domain.common

import io.ktor.http.Url
import kotlinx.datetime.LocalDateTime

data class Photo(
    val id: Id,
    val url: Url,
    val description: Description? = null,
    val uploaderId: Id,
    val uploadedAt: LocalDateTime,
) {
    init {
        require(url.protocol.name == "http" || url.protocol.name == "https") {
            "URL scheme must be http or https."
        }
    }
}
