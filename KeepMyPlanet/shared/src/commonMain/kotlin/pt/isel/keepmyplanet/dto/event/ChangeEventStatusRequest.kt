package pt.isel.keepmyplanet.dto.event

import kotlinx.serialization.Serializable
import pt.isel.keepmyplanet.domain.event.EventStatus

@Serializable
data class ChangeEventStatusRequest(
    val newStatus: EventStatus,
)
