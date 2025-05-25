package pt.isel.keepmyplanet.ui.screens.register

sealed interface RegisterEvent {
    data object NavigateToLogin : RegisterEvent

    data class ShowSnackbar(
        val message: String,
    ) : RegisterEvent
}
