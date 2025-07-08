package pt.isel.keepmyplanet.utils

import io.ktor.server.application.ApplicationCall
import io.ktor.server.auth.jwt.JWTPrincipal
import io.ktor.server.auth.principal
import pt.isel.keepmyplanet.domain.common.Id
import pt.isel.keepmyplanet.domain.user.UserRole
import pt.isel.keepmyplanet.exception.AuthenticationException
import pt.isel.keepmyplanet.exception.AuthorizationException
import pt.isel.keepmyplanet.exception.ValidationException

data class AuthPrincipal(
    val id: Id,
    val role: UserRole,
)

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

fun ApplicationCall.getQueryDoubleParameter(paramName: String): Double? =
    request.queryParameters[paramName]
        ?.toDoubleOrNull()

fun ApplicationCall.getQueryStringParameter(paramName: String): String? =
    request.queryParameters[paramName]

fun ApplicationCall.getQueryIntParameter(
    name: String,
    default: Int,
): Int = this.request.queryParameters[name]?.toIntOrNull() ?: default

fun ApplicationCall.getAuthPrincipal(): AuthPrincipal {
    val principal =
        principal<JWTPrincipal>()
            ?: throw AuthenticationException("Missing or invalid authentication token.")
    val userIdString =
        principal.payload.getClaim("userId").asString()
            ?: throw AuthenticationException("User ID not found in token.")

    val userRoleString =
        principal.payload.getClaim("role").asString()
            ?: throw AuthenticationException("User role not found in token.")

    val userIdUInt =
        userIdString.toUIntOrNull()
            ?: throw AuthenticationException("Invalid User ID format in token.")
    if (userIdUInt == 0U) throw AuthenticationException("Invalid User ID in token.")

    val userRole =
        try {
            UserRole.valueOf(userRoleString)
        } catch (e: Exception) {
            UserRole.USER
        }

    return AuthPrincipal(Id(userIdUInt), userRole)
}

fun ApplicationCall.getCurrentUserId(): Id = getAuthPrincipal().id

fun ensureAdminOrFail(principal: AuthPrincipal) {
    if (principal.role != UserRole.ADMIN) {
        throw AuthorizationException("This action requires admin privileges.")
    }
}
