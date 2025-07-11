package pt.isel.keepmyplanet.ui.chat.states

import pt.isel.keepmyplanet.domain.common.Id
import pt.isel.keepmyplanet.domain.message.Message

enum class SendStatus {
    SENDING,
    SENT,
    FAILED,
}

data class UiMessage(
    val message: Message,
    val status: SendStatus,
    val temporaryId: Id? = null,
)
