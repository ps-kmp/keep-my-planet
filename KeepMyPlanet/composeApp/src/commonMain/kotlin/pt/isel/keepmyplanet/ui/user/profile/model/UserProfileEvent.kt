package pt.isel.keepmyplanet.ui.user.profile.model

import pt.isel.keepmyplanet.ui.base.UiEvent

sealed interface UserProfileEvent : UiEvent {
    data class ShowSnackbar(
        val message: String,
    ) : UserProfileEvent

    data class ProfileUpdated(
        val userInfo: UserInfo,
    ) : UserProfileEvent

    data object AccountDeleted : UserProfileEvent
}
