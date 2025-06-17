package pt.isel.keepmyplanet.ui.event.forms.model

import pt.isel.keepmyplanet.domain.common.Id

sealed class EventFormScreenEvent {
    data class ShowSnackbar(
        val message: String,
    ) : EventFormScreenEvent()

    data class EventCreated(
        val eventId: Id,
    ) : EventFormScreenEvent()

    object EventUpdated : EventFormScreenEvent()

    object NavigateBack : EventFormScreenEvent()
}
