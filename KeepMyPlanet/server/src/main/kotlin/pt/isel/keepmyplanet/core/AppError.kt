package pt.isel.keepmyplanet.core

import pt.isel.keepmyplanet.domain.common.Id
import pt.isel.keepmyplanet.domain.user.Email

sealed interface AppError

sealed class ChatError : AppError {
    data object EventNotFound : ChatError()

    data object UserNotFound : ChatError()

    data object InvalidMessageContent : ChatError()
}

open class NotFoundException(
    type: String,
    id: Id,
) : NoSuchElementException("$type with id ${id.value} not found.")

class PhotoNotFoundInZoneException(
    zoneId: Id,
    photoId: Id,
) : NoSuchElementException("Photo ${photoId.value} is not associated with zone ${zoneId.value}.")

class ZoneUpdateException(
    message: String,
) : RuntimeException(message)

class ZoneInvalidStateException(
    message: String,
) : IllegalStateException(message)

class DuplicateEmailException(
    email: Email,
) : IllegalStateException("User with email ${email.value} already exists.")
