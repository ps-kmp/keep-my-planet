package pt.isel.keepmyplanet.ui.home.states

import pt.isel.keepmyplanet.ui.base.UiEvent

sealed interface HomeEvent : UiEvent {
    data class ShowSnackbar(
        val message: String,
    ) : HomeEvent

    data object RequestLocation : HomeEvent
}
