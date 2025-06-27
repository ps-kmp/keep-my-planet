package pt.isel.keepmyplanet.ui.zone.update.states

import pt.isel.keepmyplanet.ui.viewmodel.UiEvent


sealed interface UpdateZoneEvent : UiEvent {
    data class ShowSnackbar(val message: String) : UpdateZoneEvent
    data object UpdateSuccessful : UpdateZoneEvent
}
