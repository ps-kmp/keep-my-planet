package pt.isel.keepmyplanet.ui.chat.states

import pt.isel.keepmyplanet.ui.base.UiEvent

sealed interface ChatEvent : UiEvent {
    data class ShowSnackbar(
        val message: String,
    ) : ChatEvent

    data object ScrollToBottom : ChatEvent
}
