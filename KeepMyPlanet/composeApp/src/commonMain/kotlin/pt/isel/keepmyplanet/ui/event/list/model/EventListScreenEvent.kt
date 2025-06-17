package pt.isel.keepmyplanet.ui.event.list.model

sealed class EventListScreenEvent {
    data class ShowSnackbar(
        val message: String,
    ) : EventListScreenEvent()
}
