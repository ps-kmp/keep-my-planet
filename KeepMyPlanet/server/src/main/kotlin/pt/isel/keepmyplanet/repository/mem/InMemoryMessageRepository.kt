package pt.isel.keepmyplanet.repository.mem

import pt.isel.keepmyplanet.core.NotFoundException
import pt.isel.keepmyplanet.domain.common.Id
import pt.isel.keepmyplanet.domain.message.Message
import pt.isel.keepmyplanet.repository.MessageRepository
import pt.isel.keepmyplanet.util.now
import java.util.concurrent.ConcurrentHashMap

class InMemoryMessageRepository : MessageRepository {
    private val messagesByEvent = ConcurrentHashMap<Id, MutableList<Message>>() // eventId -> List of messages

    override suspend fun create(entity: Message): Message {
        val messages = messagesByEvent.computeIfAbsent(entity.eventId) { mutableListOf() }
        val nextPosition = messages.size
        val newMessage = entity.copy(chatPosition = nextPosition)
        messages.add(newMessage)
        return newMessage
    }

    override suspend fun getById(id: Id): Message? =
        messagesByEvent.values
            .flatten()
            .firstOrNull { it.chatPosition == id.value.toInt() }

    override suspend fun getAll(): List<Message> =
        messagesByEvent.values
            .flatten()
            .sortedWith(compareBy({ it.eventId.value }, { it.chatPosition }))

    override suspend fun update(entity: Message): Message {
        val messages =
            messagesByEvent[entity.eventId]
                ?: throw NotFoundException("Message list for event", entity.eventId)

        val index = messages.indexOfFirst { it.chatPosition == entity.chatPosition }
        if (index == -1) throw NotFoundException("Message", Id(entity.chatPosition.toUInt()))

        val updatedMessage = entity.copy(timestamp = now())
        messages[index] = updatedMessage
        return updatedMessage
    }

    override suspend fun deleteById(id: Id): Boolean {
        val chatPos = id.value.toInt()
        messagesByEvent.forEach { (_, messages) ->
            val removed = messages.removeIf { it.chatPosition == chatPos }
            if (removed) return true
        }
        return false
    }

    override suspend fun findByEventId(eventId: Id): List<Message> = messagesByEvent[eventId]?.sortedBy { it.chatPosition } ?: emptyList()

    override suspend fun findBySenderId(senderId: Id): List<Message> =
        messagesByEvent.values
            .flatten()
            .filter { it.senderId == senderId }
            .sortedWith(compareBy({ it.eventId.value }, { it.chatPosition }))

    override suspend fun deleteAllByEventId(eventId: Id): Int = messagesByEvent.remove(eventId)?.size ?: 0

    fun clear() = messagesByEvent.clear()
}
