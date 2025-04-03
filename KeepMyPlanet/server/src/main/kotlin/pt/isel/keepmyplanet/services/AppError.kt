package pt.isel.keepmyplanet.services

import pt.isel.keepmyplanet.domain.common.Id
import pt.isel.keepmyplanet.domain.user.Email

sealed interface AppError

sealed class ChatError : AppError {
    data object EventNotFound : ChatError()

    data object UserNotFound : ChatError()

    data object InvalidMessageContent : ChatError()
}

class PhotoAssociatedWithDifferentZoneException(
    photoId: Id,
    attemptedZoneId: Id,
    existingZoneId: Id,
) : IllegalStateException(
        "Photo ${photoId.value} cannot be added to zone ${attemptedZoneId.value} " +
            "because it is already associated with zone ${existingZoneId.value}.",
    )

class ZoneNotFoundException(
    id: Id,
) : NoSuchElementException("Zone with id ${id.value} not found.")

class PhotoAlreadyAddedException(
    zoneId: Id,
    photoId: Id,
) : IllegalArgumentException("Photo ${photoId.value} already added to zone ${zoneId.value}.")

class PhotoNotAssociatedException(
    zoneId: Id,
    photoId: Id,
) : IllegalArgumentException("Photo ${photoId.value} is not associated with zone ${zoneId.value}.")

class UserNotFoundException(
    id: Id,
) : NoSuchElementException("User with id ${id.value} not found.")

class DuplicateEmailException(
    email: Email,
) : IllegalArgumentException("User with email ${email.value} already exists.")

class EmailConflictException(
    email: Email,
) : IllegalArgumentException("Email ${email.value} is already in use by another user.")

class MessageNotFoundException(
    id: Id,
) : NoSuchElementException("Message with id ${id.value} not found.")

class EventNotFoundException(
    id: Id,
) : NoSuchElementException("Event with id ${id.value} not found.")

class MaxParticipantsReachedException(
    eventId: Id,
) : IllegalStateException("Maximum participants reached for event ${eventId.value}.")

class ParticipantAlreadyRegisteredException(
    eventId: Id,
    participantId: Id,
) : IllegalArgumentException("User ${participantId.value} already registered for event ${eventId.value}.")

class ParticipantNotRegisteredException(
    eventId: Id,
    participantId: Id,
) : IllegalArgumentException("User ${participantId.value} is not registered for event ${eventId.value}.")
