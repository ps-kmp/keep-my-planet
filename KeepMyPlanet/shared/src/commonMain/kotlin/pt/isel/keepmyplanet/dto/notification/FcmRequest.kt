package pt.isel.keepmyplanet.dto.notification

import kotlinx.serialization.Serializable

@Serializable
data class FcmRequest(
    val message: FcmMessage,
)
