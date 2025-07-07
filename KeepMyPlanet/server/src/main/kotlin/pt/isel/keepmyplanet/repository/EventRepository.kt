package pt.isel.keepmyplanet.repository

import kotlinx.datetime.LocalDateTime
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

    suspend fun addAttendance(
        eventId: Id,
        userId: Id,
        checkedInAt: LocalDateTime,
    )

    suspend fun hasAttended(
        eventId: Id,
        userId: Id,
    ): Boolean

    suspend fun getAttendeesIds(eventId: Id): Set<Id>

    suspend fun findEventsAttendedByUser(
        userId: Id,
        limit: Int,
        offset: Int,
    ): List<Event>

    suspend fun findEventsToStart(): List<Event>

    suspend fun countAttendedEvents(userId: Id): Long

    suspend fun calculateTotalHoursVolunteered(userId: Id): Double

    suspend fun findCompletedEventsPendingConfirmation(timeThreshold: LocalDateTime): List<Event>

    suspend fun updateTransferStatus(
        eventId: Id,
        newOrganizerId: Id,
        pendingOrganizerId: Id,
        updatedAt: LocalDateTime,
    ): Event?

    suspend fun clearPendingTransfer(
        eventId: Id,
        updatedAt: LocalDateTime,
    ): Event

    suspend fun calculateTotalHoursVolunteeredForEvent(eventId: Id): Double
}
