package pt.isel.keepmyplanet.domain.common

import kotlinx.datetime.LocalDateTime

data class Photo(
    val id: Id,
    val url: Url,
    val description: Description? = null,
    val uploaderId: Id,
    val uploadedAt: LocalDateTime,
)
