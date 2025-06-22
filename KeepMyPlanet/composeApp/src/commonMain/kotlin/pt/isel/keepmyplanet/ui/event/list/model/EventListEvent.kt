package pt.isel.keepmyplanet.ui.event.list.model

sealed interface EventListEvent {
    data class ShowSnackbar(
        val message: String,
    ) : EventListEvent
}
