package pt.isel.keepmyplanet.ui.screens.chat

sealed interface ChatEvent {
    data class ShowSnackbar(
        val message: String,
    ) : ChatEvent

    data object ScrollToBottom : ChatEvent
}
