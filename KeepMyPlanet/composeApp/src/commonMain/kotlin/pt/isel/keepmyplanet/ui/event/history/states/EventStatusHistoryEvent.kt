package pt.isel.keepmyplanet.ui.event.history.states

import pt.isel.keepmyplanet.ui.base.UiEvent

sealed class EventStatusHistoryEvent : UiEvent {
    data class ShowSnackbar(
        val message: String,
    ) : EventStatusHistoryEvent()
}
