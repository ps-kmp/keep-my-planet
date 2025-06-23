package pt.isel.keepmyplanet.ui.chat.model

import pt.isel.keepmyplanet.domain.message.Message
import pt.isel.keepmyplanet.ui.base.UiState
import pt.isel.keepmyplanet.ui.user.profile.model.UserInfo

data class ChatUiState(
    val user: UserInfo,
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
            messageInput.isNotBlank() &&
                actionState == ActionState.Idle &&
                messageInputError == null
}
