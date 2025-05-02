package pt.isel.keepmyplanet.errors

open class AppException(
    message: String,
    cause: Throwable? = null,
) : RuntimeException(message, cause)

/** 400 Bad Request */
class ValidationException(
    message: String,
    cause: Throwable? = null,
) : AppException(message, cause)

/** 401 Unauthorized */
class AuthenticationException(
    message: String,
    cause: Throwable? = null,
) : AppException(message, cause)

/** 403 Forbidden */
class AuthorizationException(
    message: String,
    cause: Throwable? = null,
) : AppException(message, cause)

/** 404 Not Found */
class NotFoundException(
    message: String,
    cause: Throwable? = null,
) : AppException(message, cause)

/** 409 Conflict */
class ConflictException(
    message: String,
    cause: Throwable? = null,
) : AppException(message, cause)

/** 500 Internal Server Exception*/
class InternalServerException(
    message: String,
    cause: Throwable? = null,
) : AppException(message, cause)
