package pt.isel.keepmyplanet.ui.event.participants.states

import pt.isel.keepmyplanet.ui.base.UiEvent

sealed interface ParticipantListEvent : UiEvent {
    data class ShowSnackbar(val message: String) : ParticipantListEvent
}
