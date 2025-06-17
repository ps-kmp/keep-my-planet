package pt.isel.keepmyplanet.ui.event.list

sealed class EventListScreenEvent {
    data class ShowSnackbar(
        val message: String,
    ) : EventListScreenEvent()
}
