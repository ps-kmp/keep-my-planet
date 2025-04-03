package pt.isel.keepmyplanet.repository.mem

import kotlinx.coroutines.flow.Flow
import pt.isel.keepmyplanet.domain.common.Id
import pt.isel.keepmyplanet.domain.message.Message
import pt.isel.keepmyplanet.repository.MessageRepository

class InMemMessageRepository : MessageRepository {
    private val messages = mutableListOf<Message>()

    override fun findByEventId(eventId: Id): List<Message> = messages.filter { it.eventId == eventId }

    override fun findBySenderId(senderId: Id): List<Message> = messages.filter { it.senderId == senderId }

    override suspend fun deleteAllByEventId(eventId: Id): Int {
        val initialSize = messages.size
        messages.removeIf { it.eventId == eventId }
        return initialSize - messages.size
    }

    override suspend fun addMessage(message: Message): Boolean {
        messages.add(message)
        return true
    }

    override suspend fun create(entity: Message): Message {
        TODO("Not yet implemented")
    }

    override suspend fun getById(id: Id): Message? {
        TODO("Not yet implemented")
    }

    override fun getAll(): Flow<List<Message>> {
        TODO("Not yet implemented")
    }

    override suspend fun update(entity: Message): Message {
        TODO("Not yet implemented")
    }

    override suspend fun deleteById(id: Id): Boolean {
        TODO("Not yet implemented")
    }
}
