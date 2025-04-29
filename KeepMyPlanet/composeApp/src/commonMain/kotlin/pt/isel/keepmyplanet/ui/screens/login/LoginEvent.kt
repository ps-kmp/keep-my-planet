package pt.isel.keepmyplanet.ui.screens.login

import pt.isel.keepmyplanet.data.model.UserSession

sealed interface LoginEvent {
    data class NavigateToHome(
        val userSession: UserSession,
    ) : LoginEvent

    data class ShowSnackbar(
        val message: String,
    ) : LoginEvent
}
