package pt.isel.keepmyplanet.domain.event

import kotlinx.datetime.LocalDateTime

data class Period(val start: LocalDateTime, val end: LocalDateTime) {
    init {
        require(start < end) { "Start time must be set before end time" }
    }
}
