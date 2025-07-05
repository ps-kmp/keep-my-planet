package pt.isel.keepmyplanet.api

import io.ktor.client.request.delete
import io.ktor.client.request.get
import io.ktor.client.request.header
import io.ktor.client.request.parameter
import io.ktor.client.request.patch
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.client.statement.bodyAsText
import io.ktor.http.ContentType
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpStatusCode
import io.ktor.http.contentType
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue
import kotlinx.serialization.json.Json
import pt.isel.keepmyplanet.domain.common.Description
import pt.isel.keepmyplanet.domain.common.Id
import pt.isel.keepmyplanet.domain.user.Email
import pt.isel.keepmyplanet.domain.zone.Location
import pt.isel.keepmyplanet.domain.zone.ZoneSeverity
import pt.isel.keepmyplanet.domain.zone.ZoneStatus
import pt.isel.keepmyplanet.dto.zone.AddPhotoRequest
import pt.isel.keepmyplanet.dto.zone.ReportZoneRequest
import pt.isel.keepmyplanet.dto.zone.UpdateZoneRequest
import pt.isel.keepmyplanet.dto.zone.ZoneResponse
import pt.isel.keepmyplanet.service.ZoneService
import pt.isel.keepmyplanet.service.ZoneStateChangeService

class ZoneWebApiTest : BaseWebApiTest() {
    private val fakeZoneStateChangeService =
        ZoneStateChangeService(
            zoneRepository = fakeZoneRepository,
            zoneStateChangeRepository = fakeZoneStateChangeRepository,
        )
    private val zoneService =
        ZoneService(
            fakeZoneRepository,
            fakeUserRepository,
            fakeEventRepository,
            fakePhotoRepository,
            fakeZoneStateChangeService,
        )

    @Test
    fun `POST zones - should create zone successfully`() =
        testApp({ zoneWebApi(zoneService) }) {
            val user = createTestUser()
            val photo1 = createTestPhoto(uploaderId = user.id)
            val photo2 = createTestPhoto(uploaderId = user.id)
            val token = generateTestToken(user.id)
            val requestBody =
                ReportZoneRequest(
                    latitude = 40.0,
                    longitude = -75.0,
                    description = "Park",
                    photoIds = setOf(photo1.id.value, photo2.id.value),
                    severity = "HIGH",
                )

            val response =
                client.post("/zones") {
                    header(HttpHeaders.Authorization, "Bearer $token")
                    contentType(ContentType.Application.Json)
                    setBody(Json.encodeToString(requestBody))
                }
            assertEquals(HttpStatusCode.Created, response.status)

            val responseBody = Json.decodeFromString<ZoneResponse>(response.bodyAsText())
            assertEquals(requestBody.latitude, responseBody.latitude)
            assertEquals(requestBody.longitude, responseBody.longitude)
            assertEquals(requestBody.description, responseBody.description)
            assertEquals(ZoneSeverity.HIGH.name, responseBody.severity)
            assertEquals(requestBody.photoIds, responseBody.photosIds)
            assertEquals(user.id.value, responseBody.reporterId)

            val createdZone = fakeZoneRepository.getById(Id(responseBody.id))
            assertNotNull(createdZone)
            assertEquals(requestBody.description, createdZone.description.value)
            assertEquals(user.id, createdZone.reporterId)
            assertEquals(ZoneSeverity.HIGH, createdZone.zoneSeverity)
            assertEquals(requestBody.photoIds.map { Id(it) }.toSet(), createdZone.photosIds)
        }

    @Test
    fun `POST zones - should fail with 400 on invalid latitude`() =
        testApp({ zoneWebApi(zoneService) }) {
            val user = createTestUser()
            val token = generateTestToken(user.id)
            val requestBody = ReportZoneRequest(200.0, -75.0, "Bad data", setOf(1u), "MEDIUM")

            val response =
                client.post("/zones") {
                    header(HttpHeaders.Authorization, "Bearer $token")
                    contentType(ContentType.Application.Json)
                    setBody(Json.encodeToString(requestBody))
                }
            assertEquals(HttpStatusCode.BadRequest, response.status)
        }

    @Test
    fun `POST zones - should fail with 400 on blank description`() =
        testApp({ zoneWebApi(zoneService) }) {
            val user = createTestUser()
            val token = generateTestToken(user.id)
            val requestBody = ReportZoneRequest(10.0, 10.0, "  ", setOf(1u), "LOW")
            val response =
                client.post("/zones") {
                    header(HttpHeaders.Authorization, "Bearer $token")
                    contentType(ContentType.Application.Json)
                    setBody(Json.encodeToString(requestBody))
                }
            assertEquals(HttpStatusCode.BadRequest, response.status)
        }

    @Test
    fun `GET zones - should return empty list when no zones exist`() =
        testApp({ zoneWebApi(zoneService) }) {
            val response = client.get("/zones")
            assertEquals(HttpStatusCode.OK, response.status)
            assertEquals("[]", response.bodyAsText())
        }

    @Test
    fun `GET zones - should return list of existing zones`() =
        testApp({ zoneWebApi(zoneService) }) {
            val user = createTestUser()
            val zone1 = createTestZone(reporterId = user.id, description = "Zone A")
            val zone2 = createTestZone(reporterId = user.id, description = "Zone B")

            val response = client.get("/zones")
            assertEquals(HttpStatusCode.OK, response.status)

            val responseList = Json.decodeFromString<List<ZoneResponse>>(response.bodyAsText())
            assertEquals(2, responseList.size)
            assertTrue(responseList.any { it.id == zone1.id.value && it.description == "Zone A" })
            assertTrue(responseList.any { it.id == zone2.id.value && it.description == "Zone B" })
        }

    @Test
    fun `GET zones with location - should return zones within radius`() =
        testApp({ zoneWebApi(zoneService) }) {
            val user = createTestUser()
            val centerLat = 50.0
            val centerLon = 10.0
            val radiusKm = 15.0
            val zoneInside = createTestZone(reporterId = user.id, location = Location(50.05, 10.0))
            val zoneOutside = createTestZone(reporterId = user.id, location = Location(50.2, 10.0))

            val response =
                client.get("/zones") {
                    parameter("lat", centerLat)
                    parameter("lon", centerLon)
                    parameter("radius", radiusKm)
                }

            assertEquals(HttpStatusCode.OK, response.status)
            val responseList = Json.decodeFromString<List<ZoneResponse>>(response.bodyAsText())
            assertEquals(1, responseList.size)
            assertEquals(zoneInside.id.value, responseList[0].id)
            assertTrue(responseList.none { it.id == zoneOutside.id.value })
        }

    @Test
    fun `GET zones with location - should return 400 if radius is missing when lat-lon provided`() =
        testApp({ zoneWebApi(zoneService) }) {
            val response =
                client.get("/zones") {
                    parameter("lat", 50.0)
                    parameter("lon", 10.0)
                }
            assertEquals(HttpStatusCode.BadRequest, response.status)
        }

    @Test
    fun `GET zones with location - should return 400 if lat is missing when lon-radius provided`() =
        testApp({ zoneWebApi(zoneService) }) {
            val response =
                client.get("/zones") {
                    parameter("lon", 10.0)
                    parameter("radius", 10.0)
                }
            assertEquals(HttpStatusCode.BadRequest, response.status)
        }

    @Test
    fun `GET zones with location - should return 400 if location coordinate is invalid`() =
        testApp({ zoneWebApi(zoneService) }) {
            val response =
                client.get("/zones") {
                    parameter("lat", 200.0)
                    parameter("lon", 10.0)
                    parameter("radius", 10.0)
                }
            assertEquals(HttpStatusCode.BadRequest, response.status)
        }

    @Test
    fun `GET zones with location - should return 400 if radius is invalid`() =
        testApp({ zoneWebApi(zoneService) }) {
            val response =
                client.get("/zones") {
                    parameter("lat", 50.0)
                    parameter("lon", 10.0)
                    parameter("radius", -5.0)
                }
            assertEquals(HttpStatusCode.BadRequest, response.status)
        }

    @Test
    fun `GET zones by ID - should return zone when found`() =
        testApp({ zoneWebApi(zoneService) }) {
            val user = createTestUser()
            val zone = createTestZone(reporterId = user.id, description = "Specific Zone")

            val response = client.get("/zones/${zone.id.value}")
            assertEquals(HttpStatusCode.OK, response.status)

            val responseBody = Json.decodeFromString<ZoneResponse>(response.bodyAsText())
            assertEquals(zone.id.value, responseBody.id)
            assertEquals("Specific Zone", responseBody.description)
        }

    @Test
    fun `GET zones by ID - should return 404 when not found`() =
        testApp({ zoneWebApi(zoneService) }) {
            val response = client.get("/zones/999")
            assertEquals(HttpStatusCode.NotFound, response.status)
        }

    @Test
    fun `GET zones by ID - should return 400 for invalid ID format`() =
        testApp({ zoneWebApi(zoneService) }) {
            val response = client.get("/zones/abc")
            assertEquals(HttpStatusCode.BadRequest, response.status)
        }

    @Test
    fun `PATCH zones by ID - should update description successfully by reporter`() =
        testApp({ zoneWebApi(zoneService) }) {
            val user = createTestUser()
            val zone = createTestZone(reporterId = user.id, description = "Old Description")
            val newDescription = "New Updated Description"
            val requestBody = UpdateZoneRequest(description = newDescription)
            val token = generateTestToken(user.id)

            val response =
                client.patch("/zones/${zone.id.value}") {
                    header(HttpHeaders.Authorization, "Bearer $token")
                    contentType(ContentType.Application.Json)
                    setBody(Json.encodeToString(requestBody))
                }
            assertEquals(HttpStatusCode.OK, response.status)

            val responseBody = Json.decodeFromString<ZoneResponse>(response.bodyAsText())
            assertEquals(newDescription, responseBody.description)

            val updatedZone = fakeZoneRepository.getById(zone.id)
            assertNotNull(updatedZone)
            assertEquals(Description(newDescription), updatedZone.description)
        }

    @Test
    fun `PATCH zones by ID - should update status successfully by reporter`() =
        testApp({ zoneWebApi(zoneService) }) {
            val user = createTestUser()
            val zone = createTestZone(reporterId = user.id, status = ZoneStatus.REPORTED)
            val newStatus = ZoneStatus.CLEANING_SCHEDULED
            val requestBody = UpdateZoneRequest(status = newStatus.name)
            val token = generateTestToken(user.id)

            val response =
                client.patch("/zones/${zone.id.value}") {
                    header(HttpHeaders.Authorization, "Bearer $token")
                    contentType(ContentType.Application.Json)
                    setBody(Json.encodeToString(requestBody))
                }
            assertEquals(HttpStatusCode.OK, response.status)

            val responseBody = Json.decodeFromString<ZoneResponse>(response.bodyAsText())
            assertEquals(newStatus.name, responseBody.status)

            val updatedZone = fakeZoneRepository.getById(zone.id)
            assertNotNull(updatedZone)
            assertEquals(newStatus, updatedZone.status)
        }

    @Test
    fun `PATCH zones by ID - should update severity successfully by reporter`() =
        testApp({ zoneWebApi(zoneService) }) {
            val user = createTestUser()
            val zone = createTestZone(reporterId = user.id, zoneSeverity = ZoneSeverity.LOW)
            val newSeverity = ZoneSeverity.HIGH
            val requestBody = UpdateZoneRequest(severity = newSeverity.name)
            val token = generateTestToken(user.id)

            val response =
                client.patch("/zones/${zone.id.value}") {
                    header(HttpHeaders.Authorization, "Bearer $token")
                    contentType(ContentType.Application.Json)
                    setBody(Json.encodeToString(requestBody))
                }
            assertEquals(HttpStatusCode.OK, response.status)

            val responseBody = Json.decodeFromString<ZoneResponse>(response.bodyAsText())
            assertEquals(newSeverity.name, responseBody.severity)

            val updatedZone = fakeZoneRepository.getById(zone.id)
            assertNotNull(updatedZone)
            assertEquals(newSeverity, updatedZone.zoneSeverity)
        }

    @Test
    fun `PATCH zones by ID - should update multiple fields successfully by reporter`() =
        testApp({ zoneWebApi(zoneService) }) {
            val user = createTestUser()
            val zone =
                createTestZone(
                    reporterId = user.id,
                    description = "Initial",
                    status = ZoneStatus.REPORTED,
                    zoneSeverity = ZoneSeverity.LOW,
                )
            val newDescription = "Updated Desc"
            val newStatus = ZoneStatus.CLEANED
            val newSeverity = ZoneSeverity.MEDIUM
            val requestBody = UpdateZoneRequest(newDescription, newStatus.name, newSeverity.name)
            val token = generateTestToken(user.id)

            val response =
                client.patch("/zones/${zone.id.value}") {
                    header(HttpHeaders.Authorization, "Bearer $token")
                    contentType(ContentType.Application.Json)
                    setBody(Json.encodeToString(requestBody))
                }
            assertEquals(HttpStatusCode.OK, response.status)
            val responseBody = Json.decodeFromString<ZoneResponse>(response.bodyAsText())
            assertEquals(newDescription, responseBody.description)
            assertEquals(newStatus.name, responseBody.status)
            assertEquals(newSeverity.name, responseBody.severity)

            val updatedZone = fakeZoneRepository.getById(zone.id)!!
            assertEquals(Description(newDescription), updatedZone.description)
            assertEquals(newStatus, updatedZone.status)
            assertEquals(newSeverity, updatedZone.zoneSeverity)
        }

    @Test
    fun `PATCH zones by ID - should return 404 if zone not found`() =
        testApp({ zoneWebApi(zoneService) }) {
            val user = createTestUser()
            val requestBody = UpdateZoneRequest(status = "CLEANED")
            val token = generateTestToken(user.id)

            val response =
                client.patch("/zones/999") {
                    header(HttpHeaders.Authorization, "Bearer $token")
                    contentType(ContentType.Application.Json)
                    setBody(Json.encodeToString(requestBody))
                }
            assertEquals(HttpStatusCode.NotFound, response.status)
        }

    @Test
    fun `PATCH zones by ID - should return 400 for invalid status value`() =
        testApp({ zoneWebApi(zoneService) }) {
            val user = createTestUser()
            val zone = createTestZone(reporterId = user.id)
            val requestBody = UpdateZoneRequest(status = "INVALID_STATUS")
            val token = generateTestToken(user.id)

            val response =
                client.patch("/zones/${zone.id.value}") {
                    header(HttpHeaders.Authorization, "Bearer $token")
                    contentType(ContentType.Application.Json)
                    setBody(Json.encodeToString(requestBody))
                }
            assertEquals(HttpStatusCode.BadRequest, response.status)
        }

    @Test
    fun `PATCH zones by ID - should return 400 for invalid severity value`() =
        testApp({ zoneWebApi(zoneService) }) {
            val user = createTestUser()
            val zone = createTestZone(reporterId = user.id)
            val requestBody = UpdateZoneRequest(severity = "INVALID_SEVERITY")
            val token = generateTestToken(user.id)

            val response =
                client.patch("/zones/${zone.id.value}") {
                    header(HttpHeaders.Authorization, "Bearer $token")
                    contentType(ContentType.Application.Json)
                    setBody(Json.encodeToString(requestBody))
                }
            assertEquals(HttpStatusCode.BadRequest, response.status)
        }

    @Test
    fun `PATCH zones by ID - should return 400 for blank description`() =
        testApp({ zoneWebApi(zoneService) }) {
            val user = createTestUser()
            val zone = createTestZone(reporterId = user.id)
            val requestBody = UpdateZoneRequest(description = "   ")
            val token = generateTestToken(user.id)

            val response =
                client.patch("/zones/${zone.id.value}") {
                    header(HttpHeaders.Authorization, "Bearer $token")
                    contentType(ContentType.Application.Json)
                    setBody(Json.encodeToString(requestBody))
                }
            assertEquals(HttpStatusCode.BadRequest, response.status)
        }

    @Test
    fun `PATCH zones by ID - should return 403 if user not authorized`() =
        testApp({ zoneWebApi(zoneService) }) {
            val reporter = createTestUser()
            val otherUser = createTestUser(email = Email("other@test.com"))
            val zone = createTestZone(reporterId = reporter.id, status = ZoneStatus.REPORTED)
            val requestBody = UpdateZoneRequest(status = "CLEANED")
            val token = generateTestToken(otherUser.id)

            val response =
                client.patch("/zones/${zone.id.value}") {
                    header(HttpHeaders.Authorization, "Bearer $token")
                    contentType(ContentType.Application.Json)
                    setBody(Json.encodeToString(requestBody))
                }
            assertEquals(HttpStatusCode.Forbidden, response.status)

            val currentZone = fakeZoneRepository.getById(zone.id)
            assertNotNull(currentZone)
            assertEquals(ZoneStatus.REPORTED, currentZone.status)
        }

    @Test
    fun `DELETE zones by ID - should return 204 when reporter deletes own reported zone`() =
        testApp({ zoneWebApi(zoneService) }) {
            val user = createTestUser()
            val zone = createTestZone(reporterId = user.id, status = ZoneStatus.REPORTED)
            val token = generateTestToken(user.id)

            val response =
                client.delete("/zones/${zone.id.value}") {
                    header(HttpHeaders.Authorization, "Bearer $token")
                }
            assertEquals(HttpStatusCode.NoContent, response.status)
            assertNull(fakeZoneRepository.getById(zone.id))
        }

    @Test
    fun `DELETE zones by ID - should return 404 when zone not found`() =
        testApp({ zoneWebApi(zoneService) }) {
            val user = createTestUser()
            val token = generateTestToken(user.id)

            val response =
                client.delete("/zones/999") {
                    header(HttpHeaders.Authorization, "Bearer $token")
                }
            assertEquals(HttpStatusCode.NotFound, response.status)
        }

    @Test
    fun `DELETE zones by ID - should return 403 when non-reporter tries to delete reported zone`() =
        testApp({ zoneWebApi(zoneService) }) {
            val reporter = createTestUser()
            val otherUser = createTestUser(email = Email("other@test.com"))
            val zone = createTestZone(reporterId = reporter.id, status = ZoneStatus.REPORTED)
            val token = generateTestToken(otherUser.id)

            val response =
                client.delete("/zones/${zone.id.value}") {
                    header(HttpHeaders.Authorization, "Bearer $token")
                }
            assertEquals(HttpStatusCode.Forbidden, response.status)
            assertNotNull(fakeZoneRepository.getById(zone.id))
        }

    @Test
    fun `DELETE zones by ID - should return 403 when zone has scheduled event`() =
        testApp({ zoneWebApi(zoneService) }) {
            val reporter = createTestUser()
            val organizer = createTestUser(email = Email("organizer@test.com"))
            val zone = createTestZone(reporterId = reporter.id)
            val event = createTestEvent(zoneId = zone.id, organizerId = organizer.id)
            val token = generateTestToken(reporter.id)

            linkEventToZone(zoneId = zone.id, eventId = event.id)

            val linkedZone = fakeZoneRepository.getById(zone.id)
            assertNotNull(linkedZone)
            assertEquals(event.id, linkedZone.eventId)

            val response =
                client.delete("/zones/${zone.id.value}") {
                    header(HttpHeaders.Authorization, "Bearer $token")
                }
            assertEquals(HttpStatusCode.Forbidden, response.status)
            assertNotNull(fakeZoneRepository.getById(zone.id))
        }

    @Test
    fun `POST zone photos - should add photo successfully by reporter`() =
        testApp({ zoneWebApi(zoneService) }) {
            val user = createTestUser()
            val initialPhoto = createTestPhoto(uploaderId = user.id)
            val initialPhotosIds = setOf(initialPhoto.id)
            val zone = createTestZone(reporterId = user.id, photosIds = initialPhotosIds)
            val newPhoto = createTestPhoto(uploaderId = user.id)
            val requestBody = AddPhotoRequest(photoId = newPhoto.id.value)
            val token = generateTestToken(user.id)

            val response =
                client.post("/zones/${zone.id.value}/photos") {
                    header(HttpHeaders.Authorization, "Bearer $token")
                    contentType(ContentType.Application.Json)
                    setBody(Json.encodeToString(requestBody))
                }
            assertEquals(HttpStatusCode.OK, response.status)

            val responseBody = Json.decodeFromString<ZoneResponse>(response.bodyAsText())
            assertTrue(responseBody.photosIds.contains(newPhoto.id.value))
            assertEquals(initialPhotosIds.size + 1, responseBody.photosIds.size)

            val updatedZone = fakeZoneRepository.getById(zone.id)
            assertNotNull(updatedZone)
            assertTrue(updatedZone.photosIds.contains(newPhoto.id))
            assertEquals(initialPhotosIds.size + 1, updatedZone.photosIds.size)
        }

    @Test
    fun `POST zone photos - should return 404 if zone not found`() =
        testApp({ zoneWebApi(zoneService) }) {
            val user = createTestUser()
            val requestBody = AddPhotoRequest(photoId = 1u)
            val token = generateTestToken(user.id)

            val response =
                client.post("/zones/999/photos") {
                    header(HttpHeaders.Authorization, "Bearer $token")
                    contentType(ContentType.Application.Json)
                    setBody(Json.encodeToString(requestBody))
                }
            assertEquals(HttpStatusCode.NotFound, response.status)
        }

    @Test
    fun `POST zone photos - should return 403 if user not authorized`() =
        testApp({ zoneWebApi(zoneService) }) {
            val reporter = createTestUser()
            val otherUser = createTestUser(email = Email("other@test.com"))
            val zone = createTestZone(reporterId = reporter.id, photosIds = setOf(Id(1u)))
            val requestBody = AddPhotoRequest(photoId = 2u)
            val token = generateTestToken(otherUser.id)

            val response =
                client.post("/zones/${zone.id.value}/photos") {
                    header(HttpHeaders.Authorization, "Bearer $token")
                    contentType(ContentType.Application.Json)
                    setBody(Json.encodeToString(requestBody))
                }
            assertEquals(HttpStatusCode.Forbidden, response.status)

            val currentZone = fakeZoneRepository.getById(zone.id)
            assertNotNull(currentZone)
            assertEquals(1, currentZone.photosIds.size)
            assertFalse(currentZone.photosIds.contains(Id(2u)))
        }

    @Test
    fun `DELETE zone photo - should remove photo successfully by reporter`() =
        testApp({ zoneWebApi(zoneService) }) {
            val photoToRemove = Id(2u)
            val initialPhotos = setOf(Id(1u), photoToRemove)
            val user = createTestUser()
            val zone = createTestZone(reporterId = user.id, photosIds = initialPhotos)
            val token = generateTestToken(user.id)

            val response =
                client.delete("/zones/${zone.id.value}/photos/${photoToRemove.value}") {
                    header(HttpHeaders.Authorization, "Bearer $token")
                }
            assertEquals(HttpStatusCode.OK, response.status)

            val responseBody = Json.decodeFromString<ZoneResponse>(response.bodyAsText())
            assertFalse(responseBody.photosIds.contains(photoToRemove.value))
            assertEquals(initialPhotos.size - 1, responseBody.photosIds.size)

            val updatedZone = fakeZoneRepository.getById(zone.id)
            assertNotNull(updatedZone)
            assertFalse(updatedZone.photosIds.contains(photoToRemove))
            assertEquals(initialPhotos.size - 1, updatedZone.photosIds.size)
        }

    @Test
    fun `DELETE zone photo - should return 404 if zone not found`() =
        testApp({ zoneWebApi(zoneService) }) {
            val user = createTestUser()
            val token = generateTestToken(user.id)

            val response =
                client.delete("/zones/999/photos/1") {
                    header(HttpHeaders.Authorization, "Bearer $token")
                }
            assertEquals(HttpStatusCode.NotFound, response.status)
        }

    @Test
    fun `DELETE zone photo - should return 404 if photo not found in zone`() =
        testApp({ zoneWebApi(zoneService) }) {
            val user = createTestUser()
            val zone = createTestZone(reporterId = user.id, photosIds = setOf(Id(1u)))
            val token = generateTestToken(user.id)

            val response =
                client.delete("/zones/${zone.id.value}/photos/2") {
                    header(HttpHeaders.Authorization, "Bearer $token")
                }
            assertEquals(HttpStatusCode.NotFound, response.status)
        }

    @Test
    fun `DELETE zone photo - should return 403 if user not authorized`() =
        testApp({ zoneWebApi(zoneService) }) {
            val reporter = createTestUser()
            val otherUser = createTestUser(email = Email("other@test.com"))
            val photoToRemove = Id(2u)
            val zone = createTestZone(reporter.id, photosIds = setOf(Id(1u), photoToRemove))
            val token = generateTestToken(otherUser.id)

            val response =
                client.delete("/zones/${zone.id.value}/photos/${photoToRemove.value}") {
                    header(HttpHeaders.Authorization, "Bearer $token")
                }
            assertEquals(HttpStatusCode.Forbidden, response.status)

            val currentZone = fakeZoneRepository.getById(zone.id)
            assertNotNull(currentZone)
            assertEquals(2, currentZone.photosIds.size)
            assertTrue(currentZone.photosIds.contains(photoToRemove))
        }

    @Test
    fun `POST zones - should fail with 401 Unauthorized if no token`() =
        testApp({ zoneWebApi(zoneService) }) {
            val requestBody = ReportZoneRequest(40.0, -75.0, "Desc", setOf(1u), "HIGH")
            val response =
                client.post("/zones") {
                    contentType(ContentType.Application.Json)
                    setBody(Json.encodeToString(requestBody))
                }
            assertEquals(HttpStatusCode.Unauthorized, response.status)
        }

    @Test
    fun `PATCH zones by ID - should fail with 401 Unauthorized if no token`() =
        testApp({ zoneWebApi(zoneService) }) {
            val zone = createTestZone()
            val requestBody = UpdateZoneRequest(description = "New Description")
            val response =
                client.patch("/zones/${zone.id.value}") {
                    contentType(ContentType.Application.Json)
                    setBody(Json.encodeToString(requestBody))
                }
            assertEquals(HttpStatusCode.Unauthorized, response.status)
        }

    @Test
    fun `DELETE zones by ID - should fail with 401 Unauthorized if no token`() =
        testApp({ zoneWebApi(zoneService) }) {
            val zone = createTestZone()
            val response = client.delete("/zones/${zone.id.value}")
            assertEquals(HttpStatusCode.Unauthorized, response.status)
        }

    @Test
    fun `POST zone photos - should fail with 401 Unauthorized if no token`() =
        testApp({ zoneWebApi(zoneService) }) {
            val zone = createTestZone()
            val requestBody = AddPhotoRequest(1u)
            val response =
                client.post("/zones/${zone.id.value}/photos") {
                    contentType(ContentType.Application.Json)
                    setBody(Json.encodeToString(requestBody))
                }
            assertEquals(HttpStatusCode.Unauthorized, response.status)
        }

    @Test
    fun `DELETE zone photo - should fail with 401 Unauthorized if no token`() =
        testApp({ zoneWebApi(zoneService) }) {
            val zone = createTestZone(photosIds = setOf(Id(1u)))
            val response = client.delete("/zones/${zone.id.value}/photos/1")
            assertEquals(HttpStatusCode.Unauthorized, response.status)
        }
}
