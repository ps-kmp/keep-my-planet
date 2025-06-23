package pt.isel.keepmyplanet.ui.zone.details.model

import pt.isel.keepmyplanet.ui.base.UiEvent

sealed interface ZoneDetailsEvent : UiEvent {
    data class ShowSnackbar(
        val message: String,
    ) : ZoneDetailsEvent
}
