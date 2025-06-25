package pt.isel.keepmyplanet.ui.zone.states

import pt.isel.keepmyplanet.ui.viewmodel.UiEvent

sealed interface ZoneDetailsEvent : UiEvent {
    data class ShowSnackbar(
        val message: String,
    ) : ZoneDetailsEvent
}
