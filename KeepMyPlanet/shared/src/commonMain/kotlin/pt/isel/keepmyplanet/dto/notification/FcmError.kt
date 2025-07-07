package pt.isel.keepmyplanet.dto.notification

import kotlinx.serialization.SerialName
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
    val details: List<FcmErrorDetailsItem>? = null,
)

@Serializable
data class FcmErrorDetailsItem(
    @SerialName("@type") val type: String? = null,
    val errorCode: String? = null,
)
