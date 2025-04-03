package pt.isel.keepmyplanet.services

sealed interface AppError

sealed class ChatError : AppError {
    data object EventNotFound : ChatError()

    data object UserNotFound : ChatError()

    data object InvalidMessageContent : ChatError()
}
