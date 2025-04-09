package pt.isel.keepmyplanet.repository.mem

import pt.isel.keepmyplanet.core.NotFoundException
import pt.isel.keepmyplanet.domain.common.Id
import pt.isel.keepmyplanet.domain.message.Message
import pt.isel.keepmyplanet.repository.MessageRepository
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.atomic.AtomicInteger

class InMemoryMessageRepository : MessageRepository {
    private val messages = ConcurrentHashMap<Id, Message>()
    private val nextId = AtomicInteger(1)

    override suspend fun create(entity: Message): Message {
        val newId = Id(nextId.getAndIncrement().toUInt())
        val newMessage = entity.copy(id = newId)
        messages[newId] = newMessage
        return newMessage
    }

    override suspend fun getById(id: Id): Message? = messages[id]

    override suspend fun getAll(): List<Message> =
        messages.values
            .toList()
            .sortedBy { it.timestamp }

    override suspend fun update(entity: Message): Message {
        if (!messages.containsKey(entity.id)) throw NotFoundException("Message", entity.id)
        messages[entity.id] = entity
        return entity
    }

    override suspend fun deleteById(id: Id): Boolean = messages.remove(id) != null

    override suspend fun findByEventId(eventId: Id): List<Message> =
        messages.values
            .filter { it.eventId == eventId }
            .sortedBy { it.timestamp }

    override suspend fun findBySenderId(senderId: Id): List<Message> =
        messages.values
            .filter { it.senderId == senderId }
            .sortedBy { it.timestamp }

    override suspend fun deleteAllByEventId(eventId: Id): Int {
        val idsToRemove = messages.entries.filter { it.value.eventId == eventId }.map { it.key }
        var deletedCount = 0
        idsToRemove.forEach { if (messages.remove(it) != null) deletedCount++ }
        return deletedCount
    }

    fun clear() {
        messages.clear()
        nextId.set(1)
    }
}
