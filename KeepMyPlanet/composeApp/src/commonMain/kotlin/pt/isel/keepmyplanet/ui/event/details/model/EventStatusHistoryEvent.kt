package pt.isel.keepmyplanet.ui.event.details.model

import pt.isel.keepmyplanet.ui.base.UiEvent

sealed class EventStatusHistoryEvent : UiEvent {
    data class ShowSnackbar(val message: String) : EventStatusHistoryEvent()
}
