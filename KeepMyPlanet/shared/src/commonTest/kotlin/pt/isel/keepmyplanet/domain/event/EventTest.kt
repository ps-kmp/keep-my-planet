package pt.isel.keepmyplanet.domain.event

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertNull
import kotlin.test.assertTrue
import kotlinx.datetime.LocalDateTime
import pt.isel.keepmyplanet.domain.common.Description
import pt.isel.keepmyplanet.domain.common.Id

class EventTest {
    private val validId = Id(1u)
    private val validTitle = Title("Community Cleanup")
    private val validDescription = Description("Annual neighborhood cleanup event")
    private val now = LocalDateTime(2000, 1, 1, 1, 1)
    private val future = LocalDateTime(2000, 1, 1, 1, 2)
    private val validPeriod = Period(now, future)
    private val validZoneId = Id(2u)
    private val validOrganizerId = Id(3u)
    private val validDateTime = LocalDateTime(2000, 1, 1, 1, 1)

    @Test
    fun `valid Event should be created with required fields`() {
        Event(
            id = validId,
            title = validTitle,
            description = validDescription,
            period = validPeriod,
            zoneId = validZoneId,
            organizerId = validOrganizerId,
            createdAt = validDateTime,
            updatedAt = validDateTime,
        )
    }

    @Test
    fun `should use default values when optional fields not provided`() {
        val event =
            Event(
                id = validId,
                title = validTitle,
                description = validDescription,
                period = validPeriod,
                zoneId = validZoneId,
                organizerId = validOrganizerId,
                createdAt = validDateTime,
                updatedAt = validDateTime,
            )

        assertEquals(EventStatus.PLANNED, event.status)
        assertNull(event.maxParticipants)
        assertTrue(event.participantsIds.isEmpty())
    }

    @Test
    fun `should accept valid optional fields`() {
        val participants = setOf(Id(4u), Id(5u))
        val event =
            Event(
                id = validId,
                title = validTitle,
                description = validDescription,
                period = validPeriod,
                zoneId = validZoneId,
                organizerId = validOrganizerId,
                persistedStatus = EventStatus.IN_PROGRESS,
                maxParticipants = 50,
                participantsIds = participants,
                createdAt = validDateTime,
                updatedAt = validDateTime,
            )
        assertEquals(EventStatus.IN_PROGRESS, event.status)
        assertEquals(50, event.maxParticipants)
        assertEquals(participants, event.participantsIds)
    }

    @Test
    fun `should reject non-positive maxParticipants`() {
        assertFailsWith<IllegalArgumentException> {
            Event(
                id = validId,
                title = validTitle,
                description = validDescription,
                period = validPeriod,
                zoneId = validZoneId,
                organizerId = validOrganizerId,
                maxParticipants = 0,
                createdAt = validDateTime,
                updatedAt = validDateTime,
            )
        }
        assertFailsWith<IllegalArgumentException> {
            Event(
                id = validId,
                title = validTitle,
                description = validDescription,
                period = validPeriod,
                zoneId = validZoneId,
                organizerId = validOrganizerId,
                maxParticipants = -5,
                createdAt = validDateTime,
                updatedAt = validDateTime,
            )
        }
    }

    @Test
    fun `properties should return correct values`() {
        val participants = setOf(Id(6u))
        val event =
            Event(
                id = validId,
                title = validTitle,
                description = validDescription,
                period = validPeriod,
                zoneId = validZoneId,
                organizerId = validOrganizerId,
                persistedStatus = EventStatus.COMPLETED,
                maxParticipants = 30,
                participantsIds = participants,
                createdAt = validDateTime,
                updatedAt = validDateTime,
            )
        assertEquals(validId, event.id)
        assertEquals(validTitle, event.title)
        assertEquals(validDescription, event.description)
        assertEquals(validPeriod, event.period)
        assertEquals(validZoneId, event.zoneId)
        assertEquals(validOrganizerId, event.organizerId)
        assertEquals(EventStatus.COMPLETED, event.status)
        assertEquals(30, event.maxParticipants)
        assertEquals(participants, event.participantsIds)
        assertEquals(validDateTime, event.createdAt)
        assertEquals(validDateTime, event.updatedAt)
    }
}
