package pt.isel.keepmyplanet.repository.mem

import pt.isel.keepmyplanet.domain.common.Id
import pt.isel.keepmyplanet.domain.event.EventStateChange
import pt.isel.keepmyplanet.errors.NotFoundException
import pt.isel.keepmyplanet.repository.EventStateChangeRepository
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.atomic.AtomicInteger

class InMemoryEventStateChangeRepository : EventStateChangeRepository {
    private val changes = ConcurrentHashMap<Id, EventStateChange>()
    private val nextId = AtomicInteger(1)

    override suspend fun create(entity: EventStateChange): EventStateChange {
        val newId = Id(nextId.getAndIncrement().toUInt())
        val newChange = entity.copy(id = newId)
        changes[newId] = newChange
        return newChange
    }

    override suspend fun update(entity: EventStateChange): EventStateChange {
        if (!changes.containsKey(entity.id)) throw NotFoundException("StateChange ${entity.id} not found.")
        changes[entity.id] = entity
        return entity
    }

    override suspend fun getById(id: Id): EventStateChange? = changes[id]

    override suspend fun getAll(
        limit: Int,
        offset: Int,
    ): List<EventStateChange> = changes.values.sortedBy { it.changeTime }

    override suspend fun deleteById(id: Id): Boolean = changes.remove(id) != null

    override suspend fun findByEventId(eventId: Id): List<EventStateChange> =
        changes.values.filter { it.eventId == eventId }.sortedBy { it.changeTime }

    override suspend fun save(eventStateChange: EventStateChange): EventStateChange =
        if (changes.containsKey(eventStateChange.id)) {
            update(eventStateChange)
        } else {
            create(eventStateChange)
        }
}
