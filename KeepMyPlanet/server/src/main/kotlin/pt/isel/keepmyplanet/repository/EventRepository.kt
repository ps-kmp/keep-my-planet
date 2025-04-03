package pt.isel.keepmyplanet.repository

import kotlinx.datetime.LocalDateTime
import pt.isel.keepmyplanet.domain.common.Id
import pt.isel.keepmyplanet.domain.common.Location
import pt.isel.keepmyplanet.domain.event.Event
import pt.isel.keepmyplanet.domain.event.EventStatus

interface EventRepository : Repository<Event, Id> {
    suspend fun findByZoneId(zoneId: Id): List<Event>

    suspend fun findByOrganizerId(organizerId: Id): List<Event>

    suspend fun findByParticipantId(participantId: Id): List<Event>

    suspend fun addParticipant(
        eventId: Id,
        participantId: Id,
    ): Event

    suspend fun removeParticipant(
        eventId: Id,
        participantId: Id,
    ): Event

    suspend fun findEvents(
        center: Location? = null,
        radius: Double? = null,
        fromDate: LocalDateTime? = null,
        toDate: LocalDateTime? = null,
        statuses: List<EventStatus>? = null,
    ): List<Event>

    suspend fun isUserRegistered(
        eventId: Id,
        userId: Id,
    ): Boolean
}
