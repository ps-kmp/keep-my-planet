package pt.isel.keepmyplanet.ui.profile.states

import pt.isel.keepmyplanet.domain.user.UserInfo
import pt.isel.keepmyplanet.ui.viewmodel.UiEvent

sealed interface UserProfileEvent : UiEvent {
    data class ShowSnackbar(
        val message: String,
    ) : UserProfileEvent

    data class ProfileUpdated(
        val userInfo: UserInfo,
    ) : UserProfileEvent

    data object AccountDeleted : UserProfileEvent
}
