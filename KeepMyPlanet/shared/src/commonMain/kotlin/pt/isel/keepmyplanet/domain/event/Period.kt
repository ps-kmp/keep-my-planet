package pt.isel.keepmyplanet.domain.event

import kotlinx.datetime.LocalDateTime
import kotlinx.serialization.Serializable

@Serializable
data class Period(
    val start: LocalDateTime,
    val end: LocalDateTime? = null,
) {
    init {
        if (end != null) {
            require(start < end) { "Start time must be set before end time" }
        }
    }
}
