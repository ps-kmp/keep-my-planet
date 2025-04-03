package pt.isel.keepmyplanet.repository.mem

import pt.isel.keepmyplanet.domain.common.Id
import pt.isel.keepmyplanet.domain.message.Message
import pt.isel.keepmyplanet.repository.MessageRepository

class InMemMessageRepository : MessageRepository {
    private val messages = mutableListOf<Message>()

    override suspend fun findByEventId(eventId: Id): List<Message> = messages.filter { it.eventId == eventId }

    override suspend fun getById(id: Id): Message? = messages.find { it.id == id }

    override suspend fun findBySenderId(senderId: Id): List<Message> = messages.filter { it.senderId == senderId }

    override suspend fun deleteAllByEventId(eventId: Id): Int {
        val initialSize = messages.size
        messages.removeIf { it.eventId == eventId }
        return initialSize - messages.size
    }

    override suspend fun create(entity: Message): Message {
        messages.add(entity)
        return entity
    }

    override suspend fun getAll(): List<Message> = messages.toList()

    override suspend fun update(entity: Message): Message {
        val index = messages.indexOfFirst { it.id == entity.id }
        messages[index] = entity
        return entity
    }

    override suspend fun deleteById(id: Id): Boolean {
        val message = messages.find { it.id == id }
        return if (message != null) {
            messages.remove(message)
            true
        } else {
            false
        }
    }
}
