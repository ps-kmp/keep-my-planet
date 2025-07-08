package pt.isel.keepmyplanet.repository.memory

import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.atomic.AtomicInteger
import pt.isel.keepmyplanet.domain.common.Id
import pt.isel.keepmyplanet.domain.event.EventStateChange
import pt.isel.keepmyplanet.domain.event.EventStateChangeDetails
import pt.isel.keepmyplanet.domain.user.Name
import pt.isel.keepmyplanet.repository.EventStateChangeRepository
import pt.isel.keepmyplanet.repository.UserRepository

class InMemoryEventStateChangeRepository(
    private val userRepository: UserRepository,
) : EventStateChangeRepository {
    private val changes = ConcurrentHashMap<Id, EventStateChange>()
    private val nextId = AtomicInteger(1)

    override suspend fun create(entity: EventStateChange): EventStateChange {
        val newId = Id(nextId.getAndIncrement().toUInt())
        val newChange = entity.copy(id = newId)
        changes[newId] = newChange
        return newChange
    }

    override suspend fun update(entity: EventStateChange): EventStateChange =
        throw UnsupportedOperationException("Updating a state change log is not permitted.")

    override suspend fun getById(id: Id): EventStateChange? = changes[id]

    override suspend fun getAll(
        limit: Int,
        offset: Int,
    ): List<EventStateChange> =
        changes.values
            .sortedBy { it.changeTime }
            .drop(offset)
            .take(limit)

    override suspend fun deleteById(id: Id): Boolean =
        throw UnsupportedOperationException("Deleting a state change log is not permitted.")

    override suspend fun findByEventId(eventId: Id): List<EventStateChange> =
        changes.values.filter { it.eventId == eventId }.sortedBy { it.changeTime }

    override suspend fun findByEventIdWithDetails(eventId: Id): List<EventStateChangeDetails> {
        val eventChanges =
            changes.values
                .filter { it.eventId == eventId }
                .sortedByDescending { it.changeTime }

        val userIds = eventChanges.map { it.changedBy }.distinct()
        val users = userRepository.findByIds(userIds).associateBy { it.id }

        return eventChanges.map { change ->
            val userName = users[change.changedBy]?.name ?: Name("Unknown User")
            EventStateChangeDetails(
                stateChange = change,
                changedByName = userName,
            )
        }
    }

    override suspend fun save(eventStateChange: EventStateChange): EventStateChange =
        if (changes.containsKey(eventStateChange.id)) {
            update(eventStateChange)
        } else {
            create(eventStateChange)
        }
}
