package pt.isel.keepmyplanet.domain.auth

import kotlinx.datetime.LocalDateTime
import pt.isel.keepmyplanet.domain.common.Id
import pt.isel.keepmyplanet.utils.now

data class Attendance(
    val eventId: Id,
    val userId: Id,
    val checkInTime: LocalDateTime,
) {
    init {
        require(checkInTime <= now()) { "Check-in time cannot be in the future" }
    }
}
