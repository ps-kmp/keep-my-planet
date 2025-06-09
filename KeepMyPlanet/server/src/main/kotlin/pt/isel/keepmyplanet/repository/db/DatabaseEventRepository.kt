package pt.isel.keepmyplanet.repository.db

import io.ktor.server.plugins.NotFoundException
import pt.isel.keepmyplanet.domain.common.Id
import pt.isel.keepmyplanet.domain.common.Location
import pt.isel.keepmyplanet.domain.event.Event
import pt.isel.keepmyplanet.domain.event.EventStatus
import pt.isel.keepmyplanet.domain.event.Period
import pt.isel.keepmyplanet.repository.EventRepository
import pt.isel.keepmyplanet.repository.ZoneRepository
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
        status = this.status,
        maxParticipants = this.max_participants,
        participantsIds = participantIds,
        createdAt = this.created_at,
        updatedAt = this.updated_at,
    )

class DatabaseEventRepository(
    private val eventQueries: EventQueries,
    private val zoneRepository: ZoneRepository,
) : EventRepository {
    private fun getEventWithParticipants(dbEvent: Events): Event {
        val participantIds =
            eventQueries
                .getParticipantIdsForEvent(dbEvent.id)
                .executeAsList()
                .toSet()
        return dbEvent.toDomainEvent(participantIds)
    }

    private fun getEventWithParticipants(eventId: Id): Event? {
        val dbEvent = eventQueries.getById(eventId).executeAsOneOrNull() ?: return null
        return getEventWithParticipants(dbEvent)
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
                        status = entity.status,
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
    ): List<Event> =
        eventQueries
            .getAll(limit.toLong(), offset.toLong())
            .executeAsList()
            .map { getEventWithParticipants(it) }

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
                        status = entity.status,
                        max_participants = entity.maxParticipants,
                        updated_at = now(),
                    ).executeAsOne()

            eventQueries.removeAllParticipantsFromEvent(entity.id)
            entity.participantsIds.forEach { eventQueries.addParticipantToEvent(entity.id, it) }
            dbEvent.toDomainEvent(entity.participantsIds)
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
    ): List<Event> =
        eventQueries
            .findByName(name, limit.toLong(), offset.toLong())
            .executeAsList()
            .map { getEventWithParticipants(it) }

    override suspend fun findByZoneAndName(
        zoneId: Id,
        name: String,
    ): List<Event> =
        eventQueries
            .findByZoneAndName(zoneId, name)
            .executeAsList()
            .map { getEventWithParticipants(it) }

    override suspend fun findByOrganizerId(organizerId: Id): List<Event> =
        eventQueries
            .findByOrganizerId(organizerId)
            .executeAsList()
            .map { getEventWithParticipants(it) }

    override suspend fun findByZoneId(zoneId: Id): List<Event> =
        eventQueries
            .findByZoneId(zoneId)
            .executeAsList()
            .map { getEventWithParticipants(it) }

    override suspend fun findByParticipantId(participantId: Id): List<Event> =
        eventQueries
            .findByParticipantId(participantId)
            .executeAsList()
            .map { getEventWithParticipants(it) }

    override suspend fun findByStatus(status: EventStatus): List<Event> =
        eventQueries
            .findByStatus(status)
            .executeAsList()
            .map { getEventWithParticipants(it) }

    override suspend fun findNearLocation(
        center: Location,
        radiusKm: Double,
    ): List<Event> {
        val nearbyZones = zoneRepository.findNearLocation(center, radiusKm)
        if (nearbyZones.isEmpty()) return emptyList()

        val nearbyZoneIds = nearbyZones.map { it.id }

        return eventQueries
            .findEventsByZoneIds(nearbyZoneIds)
            .executeAsList()
            .map { getEventWithParticipants(it) }
    }
}
