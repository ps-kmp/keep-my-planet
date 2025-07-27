package pt.isel.keepmyplanet.ui.chat.states

import pt.isel.keepmyplanet.domain.event.EventStatus
import pt.isel.keepmyplanet.domain.message.ChatInfo
import pt.isel.keepmyplanet.domain.user.UserInfo
import pt.isel.keepmyplanet.ui.base.UiState

data class ChatUiState(
    val user: UserInfo?,
    val chatInfo: ChatInfo,
    val eventStatus: EventStatus? = null,
    val messages: List<UiMessage> = emptyList(),
    val messageInput: String = "",
    val messageInputError: String? = null,
    val actionState: ActionState = ActionState.Idle,
    val isLoading: Boolean = false,
    val isLoadingMore: Boolean = false,
    val hasMoreMessages: Boolean = true,
    val error: String? = null,
) : UiState {
    sealed interface ActionState {
        data object Idle : ActionState

        data object Sending : ActionState
    }

    val isInputEnabled: Boolean
        get() =
            user != null &&
                actionState == ActionState.Idle &&
                eventStatus !in listOf(EventStatus.COMPLETED, EventStatus.CANCELLED)

    val isSendEnabled: Boolean
        get() =
            isInputEnabled &&
                messageInput.isNotBlank() &&
                messageInputError == null

    val chatDisabledReason: String?
        get() =
            when (eventStatus) {
                EventStatus.COMPLETED -> "Chat is closed because the event has ended."
                EventStatus.CANCELLED -> "Chat is disabled for cancelled events."
                else -> null
            }
}
