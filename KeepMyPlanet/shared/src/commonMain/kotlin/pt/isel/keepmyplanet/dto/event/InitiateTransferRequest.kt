package pt.isel.keepmyplanet.dto.event

import kotlinx.serialization.Serializable

@Serializable
data class InitiateTransferRequest(
    val nomineeId: UInt,
)
