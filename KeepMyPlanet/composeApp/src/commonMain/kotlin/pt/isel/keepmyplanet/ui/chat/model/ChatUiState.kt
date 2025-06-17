package pt.isel.keepmyplanet.ui.chat.model

import pt.isel.keepmyplanet.domain.message.Message
import pt.isel.keepmyplanet.ui.user.model.UserInfo

data class ChatUiState(
    val user: UserInfo,
    val chatInfo: ChatInfo,
    val messages: List<Message> = emptyList(),
    val messageInput: String = "",
    val messageInputError: String? = null,
    val isLoading: Boolean = false,
    val isSending: Boolean = false,
) {
    val isSendEnabled: Boolean
        get() = messageInput.isNotBlank() && !isSending && messageInputError == null
}
