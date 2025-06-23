package pt.isel.keepmyplanet.ui.map.model

import pt.isel.keepmyplanet.ui.base.UiEvent

sealed interface MapEvent : UiEvent {
    data class ShowSnackbar(
        val message: String,
    ) : MapEvent
}
