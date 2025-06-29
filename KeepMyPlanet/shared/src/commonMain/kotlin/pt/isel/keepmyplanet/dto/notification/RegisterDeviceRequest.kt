package pt.isel.keepmyplanet.dto.notification

import kotlinx.serialization.Serializable

@Serializable
data class RegisterDeviceRequest(
    val token: String,
    val platform: String,
)
