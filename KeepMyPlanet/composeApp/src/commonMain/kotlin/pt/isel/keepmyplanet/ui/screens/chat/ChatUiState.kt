package pt.isel.keepmyplanet.ui.screens.chat

import pt.isel.keepmyplanet.data.model.EventInfo
import pt.isel.keepmyplanet.data.model.UserInfo
import pt.isel.keepmyplanet.dto.message.MessageResponse

data class ChatUiState(
    val user: UserInfo,
    val event: EventInfo,
    val messages: List<MessageResponse> = emptyList(),
    val messageInput: String = "",
    val isLoading: Boolean = false,
    val isSending: Boolean = false,
) {
    val isSendEnabled: Boolean
        get() = messageInput.isNotBlank() && !isSending
}
