package pt.isel.keepmyplanet.ui.user.profile.model

sealed interface UserProfileEvent {
    data class ShowSnackbar(
        val message: String,
    ) : UserProfileEvent

    data class ProfileUpdated(
        val userInfo: UserInfo,
    ) : UserProfileEvent

    data object AccountDeleted : UserProfileEvent
}
