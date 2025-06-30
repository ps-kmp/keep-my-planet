package pt.isel.keepmyplanet.ui.map.states

import pt.isel.keepmyplanet.ui.base.UiEvent

sealed interface MapEvent : UiEvent {
    data class ShowSnackbar(
        val message: String,
    ) : MapEvent

    data object RequestLocation : MapEvent

    data object CenterOnUserLocation : MapEvent
}
