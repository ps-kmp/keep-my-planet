package pt.isel.keepmyplanet.repository.mem

import pt.isel.keepmyplanet.core.NotFoundException
import pt.isel.keepmyplanet.domain.common.Id
import pt.isel.keepmyplanet.domain.message.Message
import pt.isel.keepmyplanet.repository.MessageRepository
import pt.isel.keepmyplanet.util.now
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.atomic.AtomicInteger

class InMemoryMessageRepository : MessageRepository {
    private val messages = ConcurrentHashMap<Id, Message>()
    private val nextId = AtomicInteger(1)

    // Add message to an event chat
    override suspend fun create(entity: Message): Message {
        val eventMessages = messages.values.filter { it.eventId == entity.eventId }
        val nextChatPosition = (eventMessages.maxOfOrNull { it.chatPosition } ?: -1) + 1
        val newMessage =
            entity.copy(
                id = Id(nextId.getAndIncrement().toUInt()),
                chatPosition = nextChatPosition,
                timestamp = now(),
            )
        messages[newMessage.id] = newMessage
        return newMessage
    }

    // Get all messages from event chat
    override suspend fun getAllByEventId(eventId: Id): List<Message> =
        messages.values
            .filter { it.eventId == eventId }
            .sortedBy { it.chatPosition }

    // Get single message by event chat
    override suspend fun getSingleByEventIdAndSeqNum(
        eventId: Id,
        sequenceNum: Int,
    ): Message? =
        messages.values
            .find { it.eventId == eventId && it.chatPosition == sequenceNum }

    // Delete message from event chat
    override suspend fun deleteById(id: Id): Boolean = messages.remove(id) != null

    override suspend fun getById(id: Id): Message? = messages[id]

    override suspend fun getAll(): List<Message> =
        messages.values
            .sortedWith(compareBy({ it.eventId.value }, { it.chatPosition }))

    override suspend fun update(entity: Message): Message {
        messages[entity.id] ?: throw NotFoundException("Message", entity.id)
        val updatedMessage = entity.copy(timestamp = now())
        messages[updatedMessage.id] = updatedMessage
        return updatedMessage
    }

    override suspend fun getAllBySenderId(senderId: Id): List<Message> =
        messages.values
            .filter { it.senderId == senderId }
            .sortedWith(compareBy({ it.eventId.value }, { it.chatPosition }))

    override suspend fun deleteAllByEventId(eventId: Id): Int {
        var count = 0
        val keysToRemove = messages.entries.filter { it.value.eventId == eventId }.map { it.key }
        keysToRemove.forEach { if (messages.remove(it) != null) count++ }
        return count
    }

    fun clear() {
        messages.clear()
        nextId.set(1)
    }
}
