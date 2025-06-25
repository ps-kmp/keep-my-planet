package pt.isel.keepmyplanet.ui.register.states

import pt.isel.keepmyplanet.ui.viewmodel.UiEvent

sealed interface RegisterEvent : UiEvent {
    data object NavigateToLogin : RegisterEvent

    data class ShowSnackbar(
        val message: String,
    ) : RegisterEvent
}
