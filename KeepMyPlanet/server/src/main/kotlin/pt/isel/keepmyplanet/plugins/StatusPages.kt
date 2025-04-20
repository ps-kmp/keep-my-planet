package pt.isel.keepmyplanet.plugins

import io.ktor.http.HttpStatusCode
import io.ktor.server.application.Application
import io.ktor.server.application.install
import io.ktor.server.plugins.statuspages.StatusPages
import io.ktor.server.response.respond
import org.slf4j.LoggerFactory
import pt.isel.keepmyplanet.dto.common.ErrorResponse
import pt.isel.keepmyplanet.errors.AppException
import pt.isel.keepmyplanet.errors.AuthenticationException
import pt.isel.keepmyplanet.errors.AuthorizationException
import pt.isel.keepmyplanet.errors.ConflictException
import pt.isel.keepmyplanet.errors.InternalServerException
import pt.isel.keepmyplanet.errors.NotFoundException
import pt.isel.keepmyplanet.errors.ValidationException

fun Application.configureStatusPages() {
    install(StatusPages) {
        val log = LoggerFactory.getLogger("pt.isel.keepmyplanet.plugins.StatusPages")

        /** 400 Bad Request */
        exception<IllegalArgumentException> { call, cause ->
            log.warn("Illegal argument: ${cause.message}", cause)
            val status = HttpStatusCode.BadRequest
            call.respond(
                status,
                ErrorResponse(
                    status = status.value,
                    error = "Invalid Argument",
                    message = cause.message ?: "An invalid argument was provided in the request.",
                ),
            )
        }
        exception<ValidationException> { call, cause ->
            log.warn("Invalid input data: ${cause.message}")
            val status = HttpStatusCode.BadRequest
            call.respond(
                status,
                ErrorResponse(
                    status = status.value,
                    error = "Invalid Input Data",
                    message = cause.message ?: "The provided data failed validation.",
                ),
            )
        }

        /** 401 Unauthorized */
        exception<AuthenticationException> { call, cause ->
            log.warn("Authentication failed: ${cause.message}")
            val status = HttpStatusCode.Unauthorized
            call.respond(
                status,
                ErrorResponse(
                    status = status.value,
                    error = "Authentication Required",
                    message = cause.message ?: "User Authentication required",
                ),
            )
        }

        /** 403 Forbidden */
        exception<AuthorizationException> { call, cause ->
            log.warn("Authorization failed for user: ${cause.message}")
            val status = HttpStatusCode.Forbidden
            call.respond(
                status,
                ErrorResponse(
                    status = status.value,
                    error = "Forbidden",
                    message = cause.message ?: "User does not have permission for this action.",
                ),
            )
        }

        /** 404 Not Found */
        exception<NotFoundException> { call, cause ->
            log.info("Resource not found: ${cause.message}")
            val status = HttpStatusCode.NotFound
            call.respond(
                status,
                ErrorResponse(
                    status = status.value,
                    error = "Resource Not Found",
                    message = cause.message ?: "The requested resource could not be found.",
                ),
            )
        }

        /** 409 Conflict */
        exception<ConflictException> { call, cause ->
            log.warn("Resource conflict: ${cause.message}")
            val status = HttpStatusCode.Conflict
            call.respond(
                status,
                ErrorResponse(
                    status = status.value,
                    error = "Conflict",
                    message = cause.message ?: "The resource was modified by another request.",
                ),
            )
        }

        /** 500 Internal Server Exception */
        exception<InternalServerException> { call, cause ->
            log.error("Operation failed unexpectedly: ${cause.message}", cause.cause)
            val status = HttpStatusCode.InternalServerError
            call.respond(
                status,
                ErrorResponse(
                    status = status.value,
                    error = "Operation Failed",
                    message = "An internal error occurred while processing the request.",
                ),
            )
        }
        exception<AppException> { call, cause ->
            log.error("Unhandled KeepMyPlanetException: ${cause.message}", cause)
            val status = HttpStatusCode.InternalServerError
            call.respond(
                status,
                ErrorResponse(
                    status = status.value,
                    error = "Application Error",
                    message = "An unexpected application error occurred.",
                ),
            )
        }
        exception<Throwable> { call, cause ->
            log.error("Unhandled Throwable: ${cause.message}", cause)
            val status = HttpStatusCode.InternalServerError
            call.respond(
                status,
                ErrorResponse(
                    status = status.value,
                    error = "Internal Server Error",
                    message = "An unexpected internal server error occurred.",
                ),
            )
        }
    }
}
