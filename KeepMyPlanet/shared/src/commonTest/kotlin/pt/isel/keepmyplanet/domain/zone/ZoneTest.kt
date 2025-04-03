package pt.isel.keepmyplanet.domain.zone

import kotlinx.datetime.LocalDateTime
import pt.isel.keepmyplanet.domain.common.Description
import pt.isel.keepmyplanet.domain.common.Id
import pt.isel.keepmyplanet.domain.common.Location
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class ZoneTest {
    private val validId = Id(1u)
    private val validLocation = Location(38.7569, -9.1165)
    private val validDescription = Description("Public park area")
    private val validReporterId = Id(2u)
    private val validDateTime = LocalDateTime(2000, 1, 1, 1, 1)

    @Test
    fun `valid Zone should be created with required fields`() {
        Zone(
            id = validId,
            location = validLocation,
            description = validDescription,
            reporterId = validReporterId,
            createdAt = validDateTime,
            updatedAt = validDateTime,
        )
    }

    @Test
    fun `should use default values when optional fields not provided`() {
        val zone =
            Zone(
                id = validId,
                location = validLocation,
                description = validDescription,
                reporterId = validReporterId,
                createdAt = validDateTime,
                updatedAt = validDateTime,
            )
        assertEquals(ZoneStatus.REPORTED, zone.status)
        assertEquals(Severity.UNKNOWN, zone.severity)
        assertTrue(zone.photosIds.isEmpty())
    }

    @Test
    fun `should accept all optional fields`() {
        val photos = setOf(Id(3u), Id(4u))
        val zone =
            Zone(
                id = validId,
                location = validLocation,
                description = validDescription,
                reporterId = validReporterId,
                status = ZoneStatus.CLEANING_SCHEDULED,
                severity = Severity.HIGH,
                photosIds = photos,
                createdAt = validDateTime,
                updatedAt = validDateTime,
            )
        assertEquals(ZoneStatus.CLEANING_SCHEDULED, zone.status)
        assertEquals(Severity.HIGH, zone.severity)
        assertEquals(photos, zone.photosIds)
    }

    @Test
    fun `properties should return correct values`() {
        val photos = setOf(Id(5u))
        val zone =
            Zone(
                id = validId,
                location = validLocation,
                description = validDescription,
                reporterId = validReporterId,
                status = ZoneStatus.CLEANED,
                severity = Severity.MEDIUM,
                photosIds = photos,
                createdAt = validDateTime,
                updatedAt = validDateTime,
            )
        assertEquals(validId, zone.id)
        assertEquals(validLocation, zone.location)
        assertEquals(validDescription, zone.description)
        assertEquals(validReporterId, zone.reporterId)
        assertEquals(ZoneStatus.CLEANED, zone.status)
        assertEquals(Severity.MEDIUM, zone.severity)
        assertEquals(photos, zone.photosIds)
        assertEquals(validDateTime, zone.createdAt)
        assertEquals(validDateTime, zone.updatedAt)
    }
}
