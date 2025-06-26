package pt.isel.keepmyplanet.repository.database

import pt.isel.keepmyplanet.domain.common.Id
import pt.isel.keepmyplanet.domain.event.EventStateChange
import pt.isel.keepmyplanet.domain.event.EventStateChangeDetails
import pt.isel.keepmyplanet.repository.EventStateChangeRepository
import pt.isel.keepmyplanet.repository.database.mappers.toDomain
import pt.isel.keepmyplanet.repository.database.mappers.toEventStateChangeDetails
import ptiselkeepmyplanetdb.EventStateChangeQueries

class DatabaseEventStateChangeRepository(
    private val queries: EventStateChangeQueries,
) : EventStateChangeRepository {
    override suspend fun create(entity: EventStateChange): EventStateChange =
        queries
            .insert(
                event_id = entity.eventId,
                new_status = entity.newStatus,
                changed_by = entity.changedBy,
                change_time = entity.changeTime,
            ).executeAsOne()
            .toDomain()

    override suspend fun save(eventStateChange: EventStateChange): EventStateChange =
        create(
            eventStateChange,
        )

    override suspend fun getById(id: Id): EventStateChange? =
        queries.getById(id).executeAsOneOrNull()?.toDomain()

    override suspend fun getAll(
        limit: Int,
        offset: Int,
    ): List<EventStateChange> =
        throw UnsupportedOperationException("getAll not supported for state changes.")

    override suspend fun update(entity: EventStateChange): EventStateChange =
        throw UnsupportedOperationException("Updating a state change log is not permitted.")

    override suspend fun deleteById(id: Id): Boolean =
        throw UnsupportedOperationException("Deleting a state change log is not permitted.")

    override suspend fun findByEventId(eventId: Id): List<EventStateChange> =
        queries.findByEventId(eventId).executeAsList().map { it.toDomain() }

    override suspend fun findByEventIdWithDetails(eventId: Id): List<EventStateChangeDetails> =
        queries
            .findByEventIdWithUserName(eventId)
            .executeAsList()
            .map { it.toEventStateChangeDetails() }
}
