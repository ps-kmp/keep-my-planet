package pt.isel.keepmyplanet.util

import io.ktor.server.application.ApplicationCall
import io.ktor.server.auth.jwt.JWTPrincipal
import io.ktor.server.auth.principal
import pt.isel.keepmyplanet.domain.common.Id
import pt.isel.keepmyplanet.errors.AuthenticationException
import pt.isel.keepmyplanet.errors.ValidationException

fun ApplicationCall.getPathUIntId(
    paramName: String,
    description: String,
): Id {
    val idValue =
        parameters[paramName]?.toUIntOrNull()
            ?: throw ValidationException(
                "$description must be a positive integer path parameter ('$paramName').",
            )
    return Id(idValue)
}

fun ApplicationCall.getPathIntParameter(
    paramName: String,
    description: String,
): Int {
    val intValue =
        parameters[paramName]?.toIntOrNull()
            ?: throw ValidationException(
                "$description must be a valid integer path parameter ('$paramName').",
            )
    if (intValue < 0) {
        throw ValidationException(
            "$description must be a non-negative integer path parameter ('$paramName').",
        )
    }
    return intValue
}

fun ApplicationCall.getQueryDoubleParameter(paramName: String): Double? =
    request.queryParameters[paramName]
        ?.toDoubleOrNull()

fun ApplicationCall.getQueryStringParameter(paramName: String): String? =
    request
        .queryParameters[paramName]

fun ApplicationCall.getQueryIntParameter(
    name: String,
    default: Int,
): Int = this.request.queryParameters[name]?.toIntOrNull() ?: default

fun ApplicationCall.getCurrentUserId(): Id {
    val principal =
        principal<JWTPrincipal>()
            ?: throw AuthenticationException("Missing or invalid authentication token.")
    val userIdString =
        principal.payload.getClaim("userId").asString()
            ?: throw AuthenticationException("User ID not found in token.")
    val userIdUInt =
        userIdString.toUIntOrNull()
            ?: throw AuthenticationException("Invalid User ID format in token.")
    if (userIdUInt == 0U) throw AuthenticationException("Invalid User ID in token.")
    return Id(userIdUInt)
}
