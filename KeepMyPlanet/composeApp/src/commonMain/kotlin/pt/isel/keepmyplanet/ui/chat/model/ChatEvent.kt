package pt.isel.keepmyplanet.ui.chat.model

sealed interface ChatEvent {
    data class ShowSnackbar(
        val message: String,
    ) : ChatEvent

    data object ScrollToBottom : ChatEvent
}
