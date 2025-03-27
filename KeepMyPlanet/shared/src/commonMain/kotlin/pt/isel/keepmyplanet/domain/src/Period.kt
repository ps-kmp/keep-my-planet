package pt.isel.keepmyplanet.domain.src

import kotlinx.datetime.LocalDateTime

data class Period(
    val start: LocalDateTime,
    val end: LocalDateTime,
) {
    init {
        require(start < end) { "Start time must be before end time" }
    }
}
