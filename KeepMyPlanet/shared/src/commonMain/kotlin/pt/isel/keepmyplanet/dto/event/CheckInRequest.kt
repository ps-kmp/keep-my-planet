package pt.isel.keepmyplanet.dto.event

import kotlinx.serialization.Serializable

@Serializable
data class CheckInRequest(
    val userId: UInt,
)
