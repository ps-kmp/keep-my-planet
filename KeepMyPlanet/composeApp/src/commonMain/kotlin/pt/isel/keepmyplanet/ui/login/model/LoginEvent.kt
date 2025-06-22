package pt.isel.keepmyplanet.ui.login.model

import pt.isel.keepmyplanet.session.model.UserSession

sealed interface LoginEvent {
    data class ShowSnackbar(
        val message: String,
    ) : LoginEvent

    data class LoginSuccess(
        val userSession: UserSession,
    ) : LoginEvent
}
