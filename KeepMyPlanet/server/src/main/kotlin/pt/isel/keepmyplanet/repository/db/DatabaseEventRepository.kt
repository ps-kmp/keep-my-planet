package pt.isel.keepmyplanet.repository.db

import io.ktor.server.plugins.NotFoundException
import kotlinx.datetime.LocalDateTime
import pt.isel.keepmyplanet.domain.common.Id
import pt.isel.keepmyplanet.domain.event.Event
import pt.isel.keepmyplanet.domain.event.EventStatus
import pt.isel.keepmyplanet.domain.event.Period
import pt.isel.keepmyplanet.repository.EventRepository
import pt.isel.keepmyplanet.util.now
import ptiselkeepmyplanetdb.EventQueries
import ptiselkeepmyplanetdb.Events

private fun Events.toDomainEvent(participantIds: Set<Id>): Event =
    Event(
        id = this.id,
        title = this.title,
        description = this.description,
        period = Period(this.start_datetime, this.end_datetime),
        zoneId = this.zone_id,
        organizerId = this.organizer_id,
        persistedStatus = this.status,
        maxParticipants = this.max_participants,
        participantsIds = participantIds,
        createdAt = this.created_at,
        updatedAt = this.updated_at,
    )

class DatabaseEventRepository(
    private val eventQueries: EventQueries,
) : EventRepository {
    private fun mapEventsToDomain(dbEvents: List<Events>): List<Event> {
        if (dbEvents.isEmpty()) return emptyList()

        val eventIds = dbEvents.map { it.id }
        val participantsByEventId =
            eventQueries
                .getParticipantIdsForEvents(eventIds)
                .executeAsList()
                .groupBy({ it.event_id }, { it.user_id })

        return dbEvents.map { dbEvent ->
            val participantIds = participantsByEventId[dbEvent.id]?.toSet() ?: emptySet()
            dbEvent.toDomainEvent(participantIds)
        }
    }

    private fun getEventWithParticipants(eventId: Id): Event? {
        val dbEvent = eventQueries.getById(eventId).executeAsOneOrNull() ?: return null
        val participantIds =
            eventQueries
                .getParticipantIdsForEvent(dbEvent.id)
                .executeAsList()
                .toSet()
        return dbEvent.toDomainEvent(participantIds)
    }

    override suspend fun create(entity: Event): Event {
        val currentTime = now()
        val participants = entity.participantsIds + entity.organizerId
        entity.maxParticipants?.let {
            require(participants.size <= it) {
                "Number of participants exceeds maxParticipants limit."
            }
        }

        return eventQueries.transactionWithResult {
            val dbEvent =
                eventQueries
                    .insert(
                        title = entity.title,
                        description = entity.description,
                        start_datetime = entity.period.start,
                        end_datetime = entity.period.end,
                        zone_id = entity.zoneId,
                        organizer_id = entity.organizerId,
                        status = entity.getPersistedStatus(),
                        max_participants = entity.maxParticipants,
                        created_at = currentTime,
                        updated_at = currentTime,
                    ).executeAsOne()

            participants.forEach { eventQueries.addParticipantToEvent(dbEvent.id, it) }
            dbEvent.toDomainEvent(participants)
        }
    }

    override suspend fun save(event: Event): Event {
        val dbEventExists = eventQueries.getById(event.id).executeAsOneOrNull() != null

        return if (dbEventExists) {
            update(event)
        } else {
            create(event.copy(id = Id(0U)))
        }
    }

    override suspend fun getById(id: Id): Event? = getEventWithParticipants(id)

    override suspend fun getAll(
        limit: Int,
        offset: Int,
    ): List<Event> {
        val dbEvents =
            eventQueries
                .getAll(limit.toLong(), offset.toLong())
                .executeAsList()
        return mapEventsToDomain(dbEvents)
    }

    override suspend fun update(entity: Event): Event {
        eventQueries.getById(entity.id).executeAsOneOrNull()
            ?: throw NotFoundException("Event '${entity.id}' not found.")

        entity.maxParticipants?.let {
            require(entity.participantsIds.size <= it) {
                "Number of participants exceeds maxParticipants limit."
            }
        }

        return eventQueries.transactionWithResult {
            val dbEvent =
                eventQueries
                    .updateEvent(
                        id = entity.id,
                        title = entity.title,
                        description = entity.description,
                        start_datetime = entity.period.start,
                        end_datetime = entity.period.end,
                        zone_id = entity.zoneId,
                        organizer_id = entity.organizerId,
                        status = entity.getPersistedStatus(),
                        max_participants = entity.maxParticipants,
                        updated_at = now(),
                    ).executeAsOne()

            val currentParticipantIds =
                eventQueries.getParticipantIdsForEvent(entity.id).executeAsList().toSet()
            val newParticipantIds = entity.participantsIds

            val participantsToAdd = newParticipantIds - currentParticipantIds
            val participantsToRemove = currentParticipantIds - newParticipantIds

            participantsToAdd.forEach { userId ->
                eventQueries.addParticipantToEvent(entity.id, userId)
            }

            participantsToRemove.forEach { userId ->
                eventQueries.removeParticipantFromEvent(entity.id, userId)
            }

            dbEvent.toDomainEvent(newParticipantIds)
        }
    }

    override suspend fun deleteById(id: Id): Boolean {
        val deletedIdResult =
            eventQueries.transactionWithResult {
                eventQueries.removeAllParticipantsFromEvent(id)
                eventQueries.deleteByIdReturningId(id).executeAsOneOrNull()
            }
        return deletedIdResult != null
    }

    override suspend fun findByName(
        name: String,
        limit: Int,
        offset: Int,
    ): List<Event> {
        val dbEvents =
            eventQueries
                .findByName(name, limit.toLong(), offset.toLong())
                .executeAsList()
        return mapEventsToDomain(dbEvents)
    }

    override suspend fun findByZoneAndName(
        zoneId: Id,
        name: String,
    ): List<Event> {
        val dbEvents =
            eventQueries
                .findByZoneAndName(zoneId, name)
                .executeAsList()
        return mapEventsToDomain(dbEvents)
    }

    override suspend fun findByOrganizerId(
        organizerId: Id,
        limit: Int,
        offset: Int,
    ): List<Event> {
        val dbEvents =
            eventQueries
                .findByOrganizerId(organizerId, limit.toLong(), offset.toLong())
                .executeAsList()
        return mapEventsToDomain(dbEvents)
    }

    override suspend fun findByNameAndOrganizerId(
        organizerId: Id,
        name: String,
        limit: Int,
        offset: Int,
    ): List<Event> {
        val dbEvents =
            eventQueries
                .findByNameAndOrganizerId(organizerId, name, limit.toLong(), offset.toLong())
                .executeAsList()
        return mapEventsToDomain(dbEvents)
    }

    override suspend fun findByZoneId(zoneId: Id): List<Event> {
        val dbEvents =
            eventQueries
                .findByZoneId(zoneId)
                .executeAsList()
        return mapEventsToDomain(dbEvents)
    }

    override suspend fun findByParticipantId(
        participantId: Id,
        limit: Int,
        offset: Int,
    ): List<Event> {
        val dbEvents =
            eventQueries
                .findByParticipantId(participantId, limit.toLong(), offset.toLong())
                .executeAsList()
        return mapEventsToDomain(dbEvents)
    }

    override suspend fun findByNameAndParticipantId(
        participantId: Id,
        name: String,
        limit: Int,
        offset: Int,
    ): List<Event> {
        val dbEvents =
            eventQueries
                .findByNameAndParticipantId(participantId, name, limit.toLong(), offset.toLong())
                .executeAsList()
        return mapEventsToDomain(dbEvents)
    }

    override suspend fun findByStatus(status: EventStatus): List<Event> {
        val dbEvents =
            eventQueries
                .findByStatus(status)
                .executeAsList()
        return mapEventsToDomain(dbEvents)
    }

    override suspend fun findEventsByZoneIds(zoneIds: List<Id>): List<Event> {
        val dbEvents =
            eventQueries
                .findEventsByZoneIds(zoneIds)
                .executeAsList()
        return mapEventsToDomain(dbEvents)
    }

    override suspend fun addAttendance(
        eventId: Id,
        userId: Id,
        checkedInAt: LocalDateTime,
    ) {
        eventQueries.addAttendance(eventId, userId, checkedInAt)
    }

    override suspend fun hasAttended(
        eventId: Id,
        userId: Id,
    ): Boolean =
        eventQueries.getAttendanceByEventAndUser(eventId, userId).executeAsOneOrNull() != null

    override suspend fun getAttendeesIds(eventId: Id): Set<Id> =
        eventQueries.getAttendeesIdsForEvent(eventId).executeAsList().toSet()

    override suspend fun findEventsAttendedByUser(
        userId: Id,
        limit: Int,
        offset: Int,
    ): List<Event> {
        val dbEvents =
            eventQueries
                .findEventsAttendedByUser(userId, limit.toLong(), offset.toLong())
                .executeAsList()
        return mapEventsToDomain(dbEvents)
    }
}
