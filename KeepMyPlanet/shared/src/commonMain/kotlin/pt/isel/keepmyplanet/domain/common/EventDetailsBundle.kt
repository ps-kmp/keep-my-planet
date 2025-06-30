package pt.isel.keepmyplanet.domain.common

import pt.isel.keepmyplanet.domain.event.Event
import pt.isel.keepmyplanet.domain.user.UserInfo

data class EventDetailsBundle(
    val event: Event,
    val participants: List<UserInfo>,
)
