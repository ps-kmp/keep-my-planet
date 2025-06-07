package pt.isel.keepmyplanet.ui.register.model

sealed interface RegisterEvent {
    data object NavigateToLogin : RegisterEvent

    data class ShowSnackbar(
        val message: String,
    ) : RegisterEvent
}
