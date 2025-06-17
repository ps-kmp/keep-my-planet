package pt.isel.keepmyplanet.ui.login.model

sealed interface LoginEvent {
    data class ShowSnackbar(
        val message: String,
    ) : LoginEvent
}
