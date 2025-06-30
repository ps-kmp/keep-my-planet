package pt.isel.keepmyplanet.ui.event.list.states

import pt.isel.keepmyplanet.ui.base.UiEvent

sealed interface EventListEvent : UiEvent {
    data class ShowSnackbar(
        val message: String,
    ) : EventListEvent
}
