package pt.isel.keepmyplanet.ui.event.forms.model

import pt.isel.keepmyplanet.domain.common.Id

sealed interface EventFormEvent {
    data class ShowSnackbar(
        val message: String,
    ) : EventFormEvent

    data class EventCreated(
        val eventId: Id,
    ) : EventFormEvent

    data object NavigateBack : EventFormEvent
}
