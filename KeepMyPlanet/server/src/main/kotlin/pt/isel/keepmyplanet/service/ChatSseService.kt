package pt.isel.keepmyplanet.service

import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import pt.isel.keepmyplanet.domain.message.Message

class ChatSseService {
    private val _messages = MutableSharedFlow<Message>(replay = 0, extraBufferCapacity = 64)
    val messages: SharedFlow<Message> = _messages.asSharedFlow()

    suspend fun publish(message: Message) {
        _messages.emit(message)
    }
}
