package pt.isel.keepmyplanet.domain.event

import pt.isel.keepmyplanet.domain.user.Name

data class EventStateChangeDetails(
    val stateChange: EventStateChange,
    val changedByName: Name,
)
