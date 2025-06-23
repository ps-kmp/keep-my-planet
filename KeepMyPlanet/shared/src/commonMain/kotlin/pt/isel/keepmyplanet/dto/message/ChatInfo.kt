package pt.isel.keepmyplanet.dto.message

import pt.isel.keepmyplanet.domain.common.Id
import pt.isel.keepmyplanet.domain.event.Title

data class ChatInfo(
    val eventId: Id,
    val eventTitle: Title,
)
