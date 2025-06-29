package pt.isel.keepmyplanet.dto.notification

import kotlinx.serialization.Serializable

@Serializable
data class FcmMessage(
    val token: String? = null,
    val topic: String? = null,
    val data: Map<String, String>? = null,
) {
    init {
        require((token != null) xor (topic != null)) {
            "Exactly one of 'token' or 'topic' must be provided."
        }
    }
}
