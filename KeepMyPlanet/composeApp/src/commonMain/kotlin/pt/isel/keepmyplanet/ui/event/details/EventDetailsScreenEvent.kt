package pt.isel.keepmyplanet.ui.event.details

sealed class EventDetailsScreenEvent {
    data class ShowSnackbar(
        val message: String,
    ) : EventDetailsScreenEvent()

    object EventActionSuccess : EventDetailsScreenEvent()

    object EventDeleted : EventDetailsScreenEvent()
}
