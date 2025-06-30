package pt.isel.keepmyplanet.ui.event.forms.states

import pt.isel.keepmyplanet.domain.common.Id
import pt.isel.keepmyplanet.ui.base.UiEvent

sealed interface EventFormEvent : UiEvent {
    data class ShowSnackbar(
        val message: String,
    ) : EventFormEvent

    data class EventCreated(
        val eventId: Id,
    ) : EventFormEvent

    data object NavigateBack : EventFormEvent
}
