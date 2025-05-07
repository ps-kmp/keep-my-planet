package pt.isel.keepmyplanet.ui.screens.event

sealed class EventScreenEvent {
    data class ShowSnackbar(
        val message: String,
    ) : EventScreenEvent()

    data object NavigateBack : EventScreenEvent()
}
