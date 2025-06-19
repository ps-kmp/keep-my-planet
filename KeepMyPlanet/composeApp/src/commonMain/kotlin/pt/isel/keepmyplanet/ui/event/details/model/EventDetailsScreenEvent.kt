package pt.isel.keepmyplanet.ui.event.details.model

import pt.isel.keepmyplanet.ui.event.details.EventDetailsViewModel

sealed class EventDetailsScreenEvent {
    data class ShowSnackbar(
        val message: String,
    ) : EventDetailsScreenEvent()

    object EventActionSuccess : EventDetailsScreenEvent()

    object EventDeleted : EventDetailsScreenEvent()

    data class NavigateTo(
        val destination: EventDetailsViewModel.QrNavigation,
    ) : EventDetailsScreenEvent()
}
