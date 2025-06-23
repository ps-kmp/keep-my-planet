package pt.isel.keepmyplanet.ui.register.model

import pt.isel.keepmyplanet.ui.base.UiEvent

sealed interface RegisterEvent : UiEvent {
    data object NavigateToLogin : RegisterEvent

    data class ShowSnackbar(
        val message: String,
    ) : RegisterEvent
}
