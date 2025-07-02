package pt.isel.keepmyplanet.dto.event

import kotlinx.serialization.Serializable

@Serializable
data class RespondToTransferRequest(
    val accept: Boolean
)
