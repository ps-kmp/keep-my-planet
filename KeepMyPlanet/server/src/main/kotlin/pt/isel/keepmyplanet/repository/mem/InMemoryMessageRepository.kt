package pt.isel.keepmyplanet.repository.mem

import kotlinx.datetime.LocalDateTime
import pt.isel.keepmyplanet.domain.common.Id
import pt.isel.keepmyplanet.domain.message.Message
import pt.isel.keepmyplanet.repository.MessageRepository
import pt.isel.keepmyplanet.utils.nowUTC
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.atomic.AtomicInteger

class InMemoryMessageRepository : MessageRepository {
    private val messagesById = ConcurrentHashMap<Id, Message>()
    private val idCounter = AtomicInteger(1)

    private fun generateNewId(): Id = Id(idCounter.getAndIncrement().toUInt())

    override suspend fun create(entity: Message): Message {
        val messageToCreate = entity.copy(id = generateNewId(), timestamp = LocalDateTime.nowUTC)
        messagesById[messageToCreate.id] = messageToCreate
        return messageToCreate
    }

    override suspend fun getById(id: Id): Message? = messagesById[id]

    override suspend fun getAll(): List<Message> = messagesById.values.toList()

    override suspend fun update(entity: Message): Message {
        println("Can't update message. Ignored.")
        return entity
    }

    override suspend fun deleteById(id: Id): Boolean = messagesById.remove(id) != null

    override suspend fun findByEventId(eventId: Id): List<Message> =
        messagesById.values
            .filter { it.eventId == eventId }
            .toList()

    override suspend fun findBySenderId(senderId: Id): List<Message> =
        messagesById.values
            .filter { it.senderId == senderId }
            .toList()

    override suspend fun deleteAllByEventId(eventId: Id): Int {
        val idsToDelete =
            messagesById.entries
                .filter { (_, message) -> message.eventId == eventId }
                .map { (id, _) -> id }
                .toList()
        if (idsToDelete.isEmpty()) return 0
        var deleteCount = 0
        idsToDelete.forEach { if (messagesById.remove(it) != null) deleteCount++ }
        return deleteCount
    }

    fun clear() {
        messagesById.clear()
        idCounter.set(1)
    }
}
