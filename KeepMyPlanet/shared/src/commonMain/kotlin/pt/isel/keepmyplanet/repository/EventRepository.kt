package pt.isel.keepmyplanet.repository

import kotlinx.coroutines.flow.Flow
import kotlinx.datetime.LocalDateTime
import pt.isel.keepmyplanet.domain.common.Id
import pt.isel.keepmyplanet.domain.common.Location
import pt.isel.keepmyplanet.domain.event.Event
import pt.isel.keepmyplanet.domain.event.EventStatus

interface EventRepository : Repository<Event, Id> {
    fun findByZoneId(zoneId: Id): Flow<List<Event>>

    fun findByOrganizerId(organizerId: Id): Flow<List<Event>>

    fun findByParticipantId(participantId: Id): Flow<List<Event>>

    suspend fun addParticipant(eventId: Id, participantId: Id): Event

    suspend fun removeParticipant(eventId: Id, participantId: Id): Event

    fun findEvents(
        center: Location? = null,
        radius: Double? = null,
        fromDate: LocalDateTime? = null,
        toDate: LocalDateTime? = null,
        statuses: List<EventStatus>? = null,
    ): Flow<List<Event>>

    suspend fun isUserRegistered(eventId: Id, userId: Id): Boolean
}
