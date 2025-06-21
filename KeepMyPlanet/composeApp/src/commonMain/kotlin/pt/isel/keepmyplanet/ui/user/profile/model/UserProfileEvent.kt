package pt.isel.keepmyplanet.ui.user.profile.model

sealed interface UserProfileEvent {
    data class ShowSnackbar(
        val message: String,
    ) : UserProfileEvent

    data object AccountDeleted : UserProfileEvent
}
