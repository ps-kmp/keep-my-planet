package pt.isel.keepmyplanet.ui.user.model

sealed interface UserProfileEvent {
    data class ShowSnackbar(
        val message: String,
    ) : UserProfileEvent

    data object NavigateToLogin : UserProfileEvent
}
