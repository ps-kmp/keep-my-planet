package pt.isel.keepmyplanet.ui.chat.states

import pt.isel.keepmyplanet.domain.message.ChatInfo
import pt.isel.keepmyplanet.domain.message.Message
import pt.isel.keepmyplanet.domain.user.UserInfo
import pt.isel.keepmyplanet.ui.base.UiState

data class ChatUiState(
    val user: UserInfo?,
    val chatInfo: ChatInfo,
    val messages: List<Message> = emptyList(),
    val messageInput: String = "",
    val messageInputError: String? = null,
    val actionState: ActionState = ActionState.Idle,
    val isLoading: Boolean = false,
    val error: String? = null,
) : UiState {
    sealed interface ActionState {
        data object Idle : ActionState

        data object Sending : ActionState
    }

    val isSendEnabled: Boolean
        get() =
            user != null &&
                messageInput.isNotBlank() &&
                actionState == ActionState.Idle &&
                messageInputError == null
}
