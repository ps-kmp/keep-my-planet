package pt.isel.keepmyplanet.utils

sealed interface AppError {
    data class ApiFormError(
        val message: String,
    ) : AppError

    data class GeneralError(
        val message: String,
    ) : AppError
}
