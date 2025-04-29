package pt.isel.keepmyplanet.ui.screens.chat

import pt.isel.keepmyplanet.data.model.EventInfo
import pt.isel.keepmyplanet.data.model.UserInfo

data class ChatUiState(
    val user: UserInfo,
    val event: EventInfo,
    val messages: List<ChatMessageUi> = emptyList(),
    val messageInput: String = "",
    val isLoading: Boolean = false,
    val isSending: Boolean = false,
    val errorMessage: String? = null,
) {
    val isSendEnabled: Boolean
        get() = messageInput.isNotBlank() && !isSending
}
