package pt.isel.keepmyplanet.ui.screens.user

sealed interface UserProfileEvent {
    data class ShowSnackbar(
        val message: String,
    ) : UserProfileEvent

    data object NavigateToLogin : UserProfileEvent
}
