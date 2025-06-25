package pt.isel.keepmyplanet.ui.event.list.states

import pt.isel.keepmyplanet.ui.viewmodel.UiEvent

sealed interface EventListEvent : UiEvent {
    data class ShowSnackbar(
        val message: String,
    ) : EventListEvent
}
