package pt.isel.keepmyplanet.ui.app.states

import pt.isel.keepmyplanet.ui.base.UiEvent

sealed interface AppEvent : UiEvent {
    data class ShowSnackbar(
        val message: String,
    ) : AppEvent
}
