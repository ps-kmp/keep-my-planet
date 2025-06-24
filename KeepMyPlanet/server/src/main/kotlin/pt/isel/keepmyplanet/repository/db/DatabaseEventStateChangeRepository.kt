package pt.isel.keepmyplanet.repository.db

import pt.isel.keepmyplanet.domain.common.Id
import pt.isel.keepmyplanet.domain.event.EventStateChange
import pt.isel.keepmyplanet.repository.EventStateChangeRepository
import ptiselkeepmyplanetdb.EventStateChangeQueries
import ptiselkeepmyplanetdb.Event_state_changes

private fun Event_state_changes.toDomain(): EventStateChange =
    EventStateChange(
        id = this.id,
        eventId = this.event_id,
        newStatus = this.new_status,
        changedBy = this.changed_by,
        changeTime = this.change_time,
    )

class DatabaseEventStateChangeRepository(
    private val queries: EventStateChangeQueries,
) : EventStateChangeRepository {
    override suspend fun create(entity: EventStateChange): EventStateChange {
        return queries.insert(
            event_id = entity.eventId,
            new_status = entity.newStatus,
            changed_by = entity.changedBy,
            change_time = entity.changeTime,
        ).executeAsOne().toDomain()
    }

    override suspend fun save(eventStateChange: EventStateChange): EventStateChange =
        create(
            eventStateChange,
        )

    override suspend fun getById(id: Id): EventStateChange? =
        queries.getById(id).executeAsOneOrNull()?.toDomain()

    override suspend fun getAll(
        limit: Int,
        offset: Int,
    ): List<EventStateChange> {
        // Not needed for the functionalities of this repository
        throw UnsupportedOperationException("getAll not supported for state changes.")
    }

    override suspend fun update(entity: EventStateChange): EventStateChange {
        throw UnsupportedOperationException("Updating a state change log is not permitted.")
    }

    override suspend fun deleteById(id: Id): Boolean {
        throw UnsupportedOperationException("Deleting a state change log is not permitted.")
    }

    override suspend fun findByEventId(eventId: Id): List<EventStateChange> =
        queries.findByEventId(eventId).executeAsList().map { it.toDomain() }
}
