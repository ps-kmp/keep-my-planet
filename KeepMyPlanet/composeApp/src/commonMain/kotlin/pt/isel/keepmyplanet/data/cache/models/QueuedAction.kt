package pt.isel.keepmyplanet.data.cache.models

import kotlinx.serialization.Serializable

@Serializable
data class QueuedAction(
    val filename: String,
    val dataBase64: String,
)
