package pt.isel.keepmyplanet.ui.login.model

import pt.isel.keepmyplanet.dto.user.UserSession
import pt.isel.keepmyplanet.ui.base.UiEvent

sealed interface LoginEvent : UiEvent {
    data class ShowSnackbar(
        val message: String,
    ) : LoginEvent

    data class LoginSuccess(
        val userSession: UserSession,
    ) : LoginEvent
}
