package pt.isel.keepmyplanet.dto.user

import kotlinx.serialization.Serializable

@Serializable
data class UserInfoSummaryResponse(
    val id: UInt,
    val name: String,
)
