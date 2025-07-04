package pt.isel.keepmyplanet.utils

import pt.isel.keepmyplanet.exception.AuthenticationException
import pt.isel.keepmyplanet.exception.AuthorizationException
import pt.isel.keepmyplanet.exception.ConflictException
import pt.isel.keepmyplanet.exception.InternalServerException
import pt.isel.keepmyplanet.exception.NotFoundException
import pt.isel.keepmyplanet.exception.ValidationException

object ErrorHandler {
    fun map(throwable: Throwable): AppError {
        return when (throwable) {
            is AuthenticationException,
            is ValidationException,
            is ConflictException,
            ->
                AppError.ApiFormError(throwable.message ?: "An error occurred on the form.")

            is AuthorizationException,
            is NotFoundException,
            is InternalServerException,
            ->
                AppError.GeneralError(throwable.message ?: "An unexpected error occurred.")

            // Para qualquer outro erro não esperado
            else -> AppError.GeneralError("An unknown error occurred.")
        }
    }
}
