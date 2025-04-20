package pt.isel.keepmyplanet.service

import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import pt.isel.keepmyplanet.domain.message.Message

class ChatSseService {
    private val _messages = MutableSharedFlow<Message>(extraBufferCapacity = Int.MAX_VALUE)
    val messages: SharedFlow<Message> = _messages

    suspend fun publish(message: Message) {
        _messages.emit(message)
    }
}
