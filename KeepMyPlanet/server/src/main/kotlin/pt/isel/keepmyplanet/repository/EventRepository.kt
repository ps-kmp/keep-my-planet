package pt.isel.keepmyplanet.repository

import pt.isel.keepmyplanet.domain.common.Id
import pt.isel.keepmyplanet.domain.event.Event
import pt.isel.keepmyplanet.domain.event.EventStatus

interface EventRepository : Repository<Event, Id> {
    suspend fun findByOrganizerId(
        organizerId: Id,
        limit: Int,
        offset: Int,
    ): List<Event>

    suspend fun findByNameAndOrganizerId(
        organizerId: Id,
        name: String,
        limit: Int,
        offset: Int,
    ): List<Event>

    suspend fun findByParticipantId(
        participantId: Id,
        limit: Int,
        offset: Int,
    ): List<Event>

    suspend fun findByNameAndParticipantId(
        participantId: Id,
        name: String,
        limit: Int,
        offset: Int,
    ): List<Event>

    suspend fun findByStatus(status: EventStatus): List<Event>

    suspend fun save(event: Event): Event

    suspend fun findByName(
        name: String,
        limit: Int = 20,
        offset: Int = 0,
    ): List<Event>

    suspend fun findByZoneId(zoneId: Id): List<Event>

    suspend fun findEventsByZoneIds(zoneIds: List<Id>): List<Event>

    suspend fun findByZoneAndName(
        zoneId: Id,
        name: String,
    ): List<Event>
}
