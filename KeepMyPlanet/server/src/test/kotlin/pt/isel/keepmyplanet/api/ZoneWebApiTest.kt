package pt.isel.keepmyplanet.api

import io.ktor.client.request.delete
import io.ktor.client.request.get
import io.ktor.client.request.parameter
import io.ktor.client.request.patch
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.client.statement.bodyAsText
import io.ktor.http.ContentType
import io.ktor.http.HttpStatusCode
import io.ktor.http.contentType
import io.ktor.server.routing.routing
import io.ktor.server.testing.ApplicationTestBuilder
import io.ktor.server.testing.testApplication
import kotlinx.serialization.json.Json
import org.junit.jupiter.api.BeforeEach
import pt.isel.keepmyplanet.domain.common.Description
import pt.isel.keepmyplanet.domain.common.Id
import pt.isel.keepmyplanet.domain.common.Location
import pt.isel.keepmyplanet.domain.zone.Zone
import pt.isel.keepmyplanet.domain.zone.ZoneSeverity
import pt.isel.keepmyplanet.domain.zone.ZoneStatus
import pt.isel.keepmyplanet.dto.zone.AddPhotoRequest
import pt.isel.keepmyplanet.dto.zone.ReportZoneRequest
import pt.isel.keepmyplanet.dto.zone.UpdateSeverityRequest
import pt.isel.keepmyplanet.dto.zone.UpdateStatusRequest
import pt.isel.keepmyplanet.dto.zone.ZoneResponse
import pt.isel.keepmyplanet.plugins.configureSerialization
import pt.isel.keepmyplanet.plugins.configureStatusPages
import pt.isel.keepmyplanet.repository.mem.InMemoryZoneRepository
import pt.isel.keepmyplanet.service.ZoneService
import pt.isel.keepmyplanet.util.now
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue

class ZoneWebApiTest {
    private val fakeZoneRepository = InMemoryZoneRepository()
    private val zoneService = ZoneService(fakeZoneRepository)
    private val testUserId = Id(100u)

    private suspend fun createTestZone(
        reporter: Id = testUserId,
        status: ZoneStatus = ZoneStatus.REPORTED,
        zoneSeverity: ZoneSeverity = ZoneSeverity.MEDIUM,
        photos: Set<Id> = setOf(Id(1u)),
        location: Location = Location(10.0, 20.0),
        description: String = "Test Zone",
    ): Zone =
        fakeZoneRepository.create(
            Zone(
                id = Id(999u),
                location = location,
                description = Description(description),
                reporterId = reporter,
                status = status,
                zoneSeverity = zoneSeverity,
                photosIds = photos,
                createdAt = now(),
                updatedAt = now(),
            ),
        )

    @BeforeEach
    fun setup() {
        fakeZoneRepository.clear()
    }

    private fun testApp(block: suspend ApplicationTestBuilder.() -> Unit) =
        testApplication {
            application {
                configureSerialization()
                configureStatusPages()
                routing { zoneWebApi(zoneService) }
            }
            block()
        }

    @Test
    fun `POST zones - should create zone successfully`() =
        testApp {
            val requestBody =
                ReportZoneRequest(
                    latitude = 40.000,
                    longitude = -75.0000,
                    description = "Park entrance",
                    photoIds = setOf(10u, 11u),
                    severity = "HIGH",
                )

            val response =
                client.post("/zones") {
                    contentType(ContentType.Application.Json)
                    setBody(Json.encodeToString(requestBody))
                }

            assertEquals(HttpStatusCode.Created, response.status)
            val responseBody = Json.decodeFromString<ZoneResponse>(response.bodyAsText())
            assertEquals(requestBody.latitude, responseBody.latitude)
            assertEquals(requestBody.description, responseBody.description)
            assertEquals(ZoneSeverity.HIGH.name, responseBody.severity)
            assertEquals(listOf(10u, 11u), responseBody.photosIds.sorted())
            assertEquals(1u, responseBody.id)
        }

    @Test
    fun `POST zones - should fail with 400 on invalid input`() =
        testApp {
            val requestBody =
                ReportZoneRequest(
                    latitude = 200.0, // Invalid latitude
                    longitude = -75.0000,
                    description = "Bad data",
                    photoIds = setOf(1u),
                    severity = "MEDIUM",
                )
            val response =
                client.post("/zones") {
                    contentType(ContentType.Application.Json)
                    setBody(Json.encodeToString(requestBody))
                }
            assertEquals(HttpStatusCode.BadRequest, response.status)
        }

    @Test
    fun `POST zones - should fail with 400 on blank description`() =
        testApp {
            val requestBody =
                ReportZoneRequest(
                    latitude = 10.0,
                    longitude = 10.0,
                    description = "  ", // Blank
                    photoIds = setOf(1u),
                )
            val response =
                client.post("/zones") {
                    contentType(ContentType.Application.Json)
                    setBody(Json.encodeToString(requestBody))
                }
            assertEquals(HttpStatusCode.BadRequest, response.status)
        }

    @Test
    fun `GET zones - should return empty list when no zones exist`() =
        testApp {
            val response = client.get("/zones")
            assertEquals(HttpStatusCode.OK, response.status)
            assertEquals("[]", response.bodyAsText())
        }

    @Test
    fun `GET zones - should return list of existing zones`() =
        testApp {
            val zone1 = createTestZone(description = "Zone A")
            val zone2 = createTestZone(description = "Zone B")

            val response = client.get("/zones")
            assertEquals(HttpStatusCode.OK, response.status)
            val responseList = Json.decodeFromString<List<ZoneResponse>>(response.bodyAsText())

            assertEquals(2, responseList.size)
            assertTrue(responseList.any { it.id == zone1.id.value && it.description == "Zone A" })
            assertTrue(responseList.any { it.id == zone2.id.value && it.description == "Zone B" })
        }

    @Test
    fun `GET zones with location - should return zones within radius`() =
        testApp {
            val center = Location(50.0, 10.0)
            val zoneInside = createTestZone(location = Location(50.01, 10.01)) // Close
            val zoneOutside = createTestZone(location = Location(51.0, 11.0)) // Far
            val radiusKm = 15.0 // Radius include zoneInside but not zoneOutside

            val response =
                client.get("/zones") {
                    parameter("lat", center.latitude)
                    parameter("lon", center.longitude)
                    parameter("radius", radiusKm)
                }

            assertEquals(HttpStatusCode.OK, response.status)
            val responseList = Json.decodeFromString<List<ZoneResponse>>(response.bodyAsText())

            assertEquals(1, responseList.size)
            assertEquals(zoneInside.id.value, responseList[0].id)
            assertTrue(responseList.none { it.id == zoneOutside.id.value })
        }

    @Test
    fun `GET zones with location - should return 400 if radius is missing`() =
        testApp {
            val response =
                client.get("/zones") {
                    parameter("lat", 50.0)
                    parameter("lon", 10.0)
                    // Missing radius
                }
            assertEquals(HttpStatusCode.BadRequest, response.status)
        }

    @Test
    fun `GET zones with location - should return 400 if location is invalid`() =
        testApp {
            val response =
                client.get("/zones") {
                    parameter("lat", 200.0) // Invalid latitude
                    parameter("lon", 10.0)
                    parameter("radius", 10.0)
                }
            assertEquals(HttpStatusCode.BadRequest, response.status)
        }

    @Test
    fun `GET zones by ID - should return zone when found`() =
        testApp {
            val zone = createTestZone(description = "Specific Zone")

            val response = client.get("/zones/${zone.id.value}")
            assertEquals(HttpStatusCode.OK, response.status)
            val responseBody = Json.decodeFromString<ZoneResponse>(response.bodyAsText())
            assertEquals(zone.id.value, responseBody.id)
            assertEquals("Specific Zone", responseBody.description)
        }

    @Test
    fun `GET zones by ID - should return 404 when not found`() =
        testApp {
            val response = client.get("/zones/999") // ID does not exist
            assertEquals(HttpStatusCode.NotFound, response.status)
        }

    @Test
    fun `GET zones by ID - should return 400 for invalid ID format`() =
        testApp {
            val response = client.get("/zones/abc")
            assertEquals(HttpStatusCode.BadRequest, response.status)
        }

    @Test
    fun `DELETE zones by ID - should return 204 when successful`() =
        testApp {
            val zone = createTestZone()

            val response = client.delete("/zones/${zone.id.value}")

            assertEquals(HttpStatusCode.NoContent, response.status)
            assertNull(fakeZoneRepository.getById(zone.id))
        }

    @Test
    fun `DELETE zones by ID - should return 404 when not found`() =
        testApp {
            val response = client.delete("/zones/999")

            assertEquals(HttpStatusCode.NotFound, response.status)
        }

    @Test
    fun `DELETE zones by ID - should return 409 when deletion is forbidden`() =
        testApp {
            val zone = createTestZone(status = ZoneStatus.CLEANING_SCHEDULED)
            val nonDeletableZone = zone.copy(eventId = Id(50u))
            fakeZoneRepository.update(nonDeletableZone)

            val response = client.delete("/zones/${zone.id.value}")
            assertEquals(HttpStatusCode.Conflict, response.status)
        }

    @Test
    fun `PATCH zone status - should update status successfully`() =
        testApp {
            val zone = createTestZone(status = ZoneStatus.REPORTED)
            val requestBody = UpdateStatusRequest(status = "CLEANING_SCHEDULED")

            val response =
                client.patch("/zones/${zone.id.value}/status") {
                    contentType(ContentType.Application.Json)
                    setBody(Json.encodeToString(requestBody))
                }

            assertEquals(HttpStatusCode.OK, response.status)
            val responseBody = Json.decodeFromString<ZoneResponse>(response.bodyAsText())
            assertEquals(ZoneStatus.CLEANING_SCHEDULED.name, responseBody.status)
        }

    @Test
    fun `PATCH zone status - should return 404 if zone not found`() =
        testApp {
            val requestBody = UpdateStatusRequest(status = "CLEANED")
            val response =
                client.patch("/zones/999/status") {
                    contentType(ContentType.Application.Json)
                    setBody(Json.encodeToString(requestBody))
                }
            assertEquals(HttpStatusCode.NotFound, response.status)
        }

    @Test
    fun `PATCH zone status - should return 400 for invalid status value`() =
        testApp {
            val zone = createTestZone()
            val requestBody = UpdateStatusRequest(status = "INVALID_STATUS")
            val response =
                client.patch("/zones/${zone.id.value}/status") {
                    contentType(ContentType.Application.Json)
                    setBody(Json.encodeToString(requestBody))
                }
            assertEquals(HttpStatusCode.BadRequest, response.status)
        }

    @Test
    fun `PATCH zone severity - should update severity successfully`() =
        testApp {
            val zone = createTestZone(zoneSeverity = ZoneSeverity.LOW)
            val requestBody = UpdateSeverityRequest(severity = "HIGH")

            val response =
                client.patch("/zones/${zone.id.value}/severity") {
                    contentType(ContentType.Application.Json)
                    setBody(Json.encodeToString(requestBody))
                }

            assertEquals(HttpStatusCode.OK, response.status)
            val responseBody = Json.decodeFromString<ZoneResponse>(response.bodyAsText())
            assertEquals(ZoneSeverity.HIGH.name, responseBody.severity)
            assertEquals(ZoneSeverity.HIGH, fakeZoneRepository.getById(zone.id)?.zoneSeverity)
        }

    @Test
    fun `PATCH zone severity - should return 400 for invalid severity value`() =
        testApp {
            val zone = createTestZone()
            val requestBody = UpdateSeverityRequest(severity = "SUPER_HIGH")
            val response =
                client.patch("/zones/${zone.id.value}/severity") {
                    contentType(ContentType.Application.Json)
                    setBody(Json.encodeToString(requestBody))
                }
            assertEquals(HttpStatusCode.BadRequest, response.status)
        }

    @Test
    fun `POST zone photos - should add photo successfully`() =
        testApp {
            val initialPhotos = setOf(Id(1u))
            val zone = createTestZone(photos = initialPhotos)
            val newPhotoId = Id(2u)
            val requestBody = AddPhotoRequest(photoId = newPhotoId.value)

            val response =
                client.post("/zones/${zone.id.value}/photos") {
                    contentType(ContentType.Application.Json)
                    setBody(Json.encodeToString(requestBody))
                }

            assertEquals(HttpStatusCode.OK, response.status)
            val responseBody = Json.decodeFromString<ZoneResponse>(response.bodyAsText())
            assertTrue(responseBody.photosIds.contains(newPhotoId.value))
            assertEquals(initialPhotos.size + 1, responseBody.photosIds.size)
            assertTrue(
                fakeZoneRepository.getById(zone.id)?.photosIds?.contains(newPhotoId) ?: false,
            )
        }

    @Test
    fun `POST zone photos - should return 404 if zone not found`() =
        testApp {
            val requestBody = AddPhotoRequest(photoId = 1u)
            val response =
                client.post("/zones/999/photos") {
                    contentType(ContentType.Application.Json)
                    setBody(Json.encodeToString(requestBody))
                }
            assertEquals(HttpStatusCode.NotFound, response.status)
        }

    @Test
    fun `DELETE zone photo - should remove photo successfully`() =
        testApp {
            val photoToRemove = Id(2u)
            val initialPhotos = setOf(Id(1u), photoToRemove)
            val zone = createTestZone(photos = initialPhotos)

            val response = client.delete("/zones/${zone.id.value}/photos/${photoToRemove.value}")

            assertEquals(HttpStatusCode.OK, response.status)
            val responseBody = Json.decodeFromString<ZoneResponse>(response.bodyAsText())
            assertFalse(responseBody.photosIds.contains(photoToRemove.value))
            assertEquals(initialPhotos.size - 1, responseBody.photosIds.size)
            assertFalse(
                fakeZoneRepository.getById(zone.id)?.photosIds?.contains(photoToRemove) ?: true,
            )
        }

    @Test
    fun `DELETE zone photo - should return 404 if zone not found`() =
        testApp {
            val response = client.delete("/zones/999/photos/1")

            assertEquals(HttpStatusCode.NotFound, response.status)
        }

    @Test
    fun `DELETE zone photo - should return 404 if photo not found in zone`() =
        testApp {
            val zone = createTestZone(photos = setOf(Id(1u))) // Photo ID 2 does not exist
            val response = client.delete("/zones/${zone.id.value}/photos/2")
            assertEquals(HttpStatusCode.NotFound, response.status)
        }

    @Test
    fun `DELETE zone photo - should return 400 for invalid photo ID format`() =
        testApp {
            val zone = createTestZone()
            val response = client.delete("/zones/${zone.id.value}/photos/abc")

            assertEquals(HttpStatusCode.BadRequest, response.status)
        }
}
