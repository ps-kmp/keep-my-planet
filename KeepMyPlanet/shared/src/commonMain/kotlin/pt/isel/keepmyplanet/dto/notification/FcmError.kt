package pt.isel.keepmyplanet.dto.notification

import kotlinx.serialization.Serializable

@Serializable
data class FcmErrorResponse(
    val error: FcmErrorDetail,
)

@Serializable
data class FcmErrorDetail(
    val code: Int,
    val message: String,
    val status: String,
)
