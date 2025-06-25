package pt.isel.keepmyplanet.api

import io.ktor.client.request.delete
import io.ktor.client.request.get
import io.ktor.client.request.header
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
import kotlin.time.Duration.Companion.days
import kotlin.time.Duration.Companion.hours
import kotlinx.serialization.json.Json
import pt.isel.keepmyplanet.domain.common.Id
import pt.isel.keepmyplanet.domain.event.EventStatus
import pt.isel.keepmyplanet.domain.user.Email
import pt.isel.keepmyplanet.domain.user.Name
import pt.isel.keepmyplanet.domain.zone.Location
import pt.isel.keepmyplanet.domain.zone.ZoneStatus
import pt.isel.keepmyplanet.dto.event.CreateEventRequest
import pt.isel.keepmyplanet.dto.event.EventResponse
import pt.isel.keepmyplanet.dto.event.UpdateEventRequest
import pt.isel.keepmyplanet.dto.user.UserResponse
import pt.isel.keepmyplanet.service.EventService
import pt.isel.keepmyplanet.service.EventStateChangeService
import pt.isel.keepmyplanet.utils.minus
import pt.isel.keepmyplanet.utils.now
import pt.isel.keepmyplanet.utils.plus

class EventWebApiTest : BaseWebApiTest() {
    private val eventService =
        EventService(
            eventRepository = fakeEventRepository,
            userRepository = fakeUserRepository,
            zoneRepository = fakeZoneRepository,
            messageRepository = fakeMessageRepository,
        )
    private val eventChangeStateService =
        EventStateChangeService(
            eventRepository = fakeEventRepository,
            zoneRepository = fakeZoneRepository,
            eventStateChangeRepository = fakeEventStateChangeRepository,
            userRepository = fakeUserRepository,
        )

    private val futureStart = now().plus(7.days)
    private val futureEnd = futureStart.plus(3.hours)

    @Test
    fun `POST events - should create event successfully`() =
        testApp({ eventWebApi(eventService, eventChangeStateService) }) {
            val organizer = createTestUser(name = Name("Organizer User"))
            val zone = createTestZone(reporterId = organizer.id)
            val token = generateTestToken(organizer.id)

            val requestBody =
                CreateEventRequest(
                    title = "New Beach Cleanup",
                    description = "Let's clean the beach!",
                    startDate = futureStart.toString(),
                    endDate = futureEnd.toString(),
                    zoneId = zone.id.value,
                    maxParticipants = 50,
                )

            val response =
                client.post("/events") {
                    header(HttpHeaders.Authorization, "Bearer $token")
                    contentType(ContentType.Application.Json)
                    setBody(Json.encodeToString(requestBody))
                }

            assertEquals(HttpStatusCode.Created, response.status)
            val responseBody = Json.decodeFromString<EventResponse>(response.bodyAsText())

            assertEquals(requestBody.title, responseBody.title)
            assertEquals(requestBody.description, responseBody.description)
            assertEquals(requestBody.startDate, responseBody.startDate)
            assertEquals(requestBody.endDate, responseBody.endDate)
            assertEquals(requestBody.zoneId, responseBody.zoneId)
            assertEquals(organizer.id.value, responseBody.organizerId)
            assertEquals(EventStatus.PLANNED.name, responseBody.status)
            assertEquals(requestBody.maxParticipants, responseBody.maxParticipants)

            val createdEvent = fakeEventRepository.getById(Id(responseBody.id))
            assertNotNull(createdEvent)
            assertEquals(requestBody.title, createdEvent.title.value)
            assertEquals(zone.id, createdEvent.zoneId)
            assertEquals(organizer.id, createdEvent.organizerId)

            val updatedZone = fakeZoneRepository.getById(zone.id)
            assertNotNull(updatedZone)
            assertEquals(createdEvent.id, updatedZone.eventId)
            assertEquals(ZoneStatus.CLEANING_SCHEDULED, updatedZone.status)
        }

    @Test
    fun `POST events - should fail with 400 for invalid date format in startDate`() =
        testApp({ eventWebApi(eventService, eventChangeStateService) }) {
            val organizer = createTestUser()
            val zone = createTestZone(reporterId = organizer.id)
            val token = generateTestToken(organizer.id)
            val requestBody =
                CreateEventRequest(
                    title = "Event",
                    description = "Desc",
                    startDate = "invalid-date",
                    endDate = futureEnd.toString(),
                    zoneId = zone.id.value,
                )
            val response =
                client.post("/events") {
                    header(HttpHeaders.Authorization, "Bearer $token")
                    contentType(ContentType.Application.Json)
                    setBody(Json.encodeToString(requestBody))
                }
            assertEquals(HttpStatusCode.BadRequest, response.status)
        }

    @Test
    fun `POST events - should fail with 400 for invalid date format in endDate`() =
        testApp({ eventWebApi(eventService, eventChangeStateService) }) {
            val organizer = createTestUser()
            val zone = createTestZone(reporterId = organizer.id)
            val token = generateTestToken(organizer.id)
            val requestBody =
                CreateEventRequest(
                    title = "Event",
                    description = "Desc",
                    startDate = futureStart.toString(),
                    endDate = "invalid-date",
                    zoneId = zone.id.value,
                )
            val response =
                client.post("/events") {
                    header(HttpHeaders.Authorization, "Bearer $token")
                    contentType(ContentType.Application.Json)
                    setBody(Json.encodeToString(requestBody))
                }
            assertEquals(HttpStatusCode.BadRequest, response.status)
        }

    @Test
    fun `POST events - should fail with 400 for start date after end date`() =
        testApp({ eventWebApi(eventService, eventChangeStateService) }) {
            val organizer = createTestUser()
            val zone = createTestZone(reporterId = organizer.id)
            val token = generateTestToken(organizer.id)
            val requestBody =
                CreateEventRequest(
                    title = "Event",
                    description = "Desc",
                    startDate = futureEnd.toString(),
                    endDate = futureStart.toString(),
                    zoneId = zone.id.value,
                )
            val response =
                client.post("/events") {
                    header(HttpHeaders.Authorization, "Bearer $token")
                    contentType(ContentType.Application.Json)
                    setBody(Json.encodeToString(requestBody))
                }
            assertEquals(HttpStatusCode.BadRequest, response.status)
        }

    @Test
    fun `POST events - should fail with 400 for start date in the past`() =
        testApp({ eventWebApi(eventService, eventChangeStateService) }) {
            val organizer = createTestUser()
            val zone = createTestZone(reporterId = organizer.id)
            val token = generateTestToken(organizer.id)
            val pastDate = now().minus(1.days).toString()
            val requestBody =
                CreateEventRequest(
                    title = "Past Event",
                    description = "Desc",
                    startDate = pastDate,
                    endDate = futureEnd.toString(),
                    zoneId = zone.id.value,
                )
            val response =
                client.post("/events") {
                    header(HttpHeaders.Authorization, "Bearer $token")
                    contentType(ContentType.Application.Json)
                    setBody(Json.encodeToString(requestBody))
                }
            assertEquals(HttpStatusCode.BadRequest, response.status)
        }

    @Test
    fun `POST events - should fail with 404 if zone not found`() =
        testApp({ eventWebApi(eventService, eventChangeStateService) }) {
            val organizer = createTestUser()
            val token = generateTestToken(organizer.id)
            val nonExistentZoneId = 999U
            val requestBody =
                CreateEventRequest(
                    title = "Event",
                    description = "Desc",
                    startDate = futureStart.toString(),
                    endDate = futureEnd.toString(),
                    zoneId = nonExistentZoneId,
                )
            val response =
                client.post("/events") {
                    header(HttpHeaders.Authorization, "Bearer $token")
                    contentType(ContentType.Application.Json)
                    setBody(Json.encodeToString(requestBody))
                }
            assertEquals(HttpStatusCode.NotFound, response.status)
        }

    @Test
    fun `POST events - should fail with 404 if organizer (current user) not found`() =
        testApp({ eventWebApi(eventService, eventChangeStateService) }) {
            val nonExistentOrganizerId = Id(998U)
            val token = generateTestToken(nonExistentOrganizerId)
            val zone = createTestZone()
            val requestBody =
                CreateEventRequest(
                    title = "Event",
                    description = "Desc",
                    startDate = futureStart.toString(),
                    endDate = futureEnd.toString(),
                    zoneId = zone.id.value,
                )
            val response =
                client.post("/events") {
                    header(HttpHeaders.Authorization, "Bearer $token")
                    contentType(ContentType.Application.Json)
                    setBody(Json.encodeToString(requestBody))
                }
            assertEquals(HttpStatusCode.NotFound, response.status)
        }

    @Test
    fun `POST events - should fail with 409 if zone already has an active event`() =
        testApp({ eventWebApi(eventService, eventChangeStateService) }) {
            val organizer = createTestUser()
            val zone = createTestZone(reporterId = organizer.id)
            val token = generateTestToken(organizer.id)
            val existingEvent =
                createTestEvent(
                    zoneId = zone.id,
                    organizerId = organizer.id,
                    status = EventStatus.PLANNED,
                )
            linkEventToZone(zone.id, existingEvent.id)

            val requestBody =
                CreateEventRequest(
                    title = "Another Event",
                    description = "Desc",
                    startDate = futureStart.plus(1.days).toString(),
                    endDate = futureEnd.plus(1.days).toString(),
                    zoneId = zone.id.value,
                )
            val response =
                client.post("/events") {
                    header(HttpHeaders.Authorization, "Bearer $token")
                    contentType(ContentType.Application.Json)
                    setBody(Json.encodeToString(requestBody))
                }
            assertEquals(HttpStatusCode.Conflict, response.status)
        }

    @Test
    fun `GET events - should return empty list if no events`() =
        testApp({ eventWebApi(eventService, eventChangeStateService) }) {
            fakeEventRepository.clear()
            val response = client.get("/events")
            assertEquals(HttpStatusCode.OK, response.status)
            assertEquals("[]", response.bodyAsText())
        }

    @Test
    fun `GET events - should return all events`() =
        testApp({ eventWebApi(eventService, eventChangeStateService) }) {
            val user = createTestUser()
            val zone1 = createTestZone(reporterId = user.id)
            val zone2 = createTestZone(reporterId = user.id, location = Location(1.0, 1.0))
            val event1 = createTestEvent(zone1.id, organizerId = user.id, title = "Event 1")
            val event2 = createTestEvent(zone2.id, organizerId = user.id, title = "Event 2")

            val response = client.get("/events")
            assertEquals(HttpStatusCode.OK, response.status)
            val responseList = Json.decodeFromString<List<EventResponse>>(response.bodyAsText())
            assertEquals(2, responseList.size)
            assertTrue(responseList.any { it.id == event1.id.value })
            assertTrue(responseList.any { it.id == event2.id.value })
        }

    @Test
    fun `GET event by ID - should return event details`() =
        testApp({ eventWebApi(eventService, eventChangeStateService) }) {
            val user = createTestUser()
            val zone = createTestZone(reporterId = user.id)
            val event = createTestEvent(zone.id, organizerId = user.id, title = "Specific Event")

            val response = client.get("/events/${event.id.value}")
            assertEquals(HttpStatusCode.OK, response.status)
            val responseBody = Json.decodeFromString<EventResponse>(response.bodyAsText())
            assertEquals(event.id.value, responseBody.id)
            assertEquals("Specific Event", responseBody.title)
        }

    @Test
    fun `GET event by ID - should return 404 if event not found`() =
        testApp({ eventWebApi(eventService, eventChangeStateService) }) {
            val response = client.get("/events/999")
            assertEquals(HttpStatusCode.NotFound, response.status)
        }

    @Test
    fun `GET event by ID - should return 400 for invalid ID format`() =
        testApp({ eventWebApi(eventService, eventChangeStateService) }) {
            val response = client.get("/events/abc")
            assertEquals(HttpStatusCode.BadRequest, response.status)
        }

    @Test
    fun `PATCH event by ID - should update event successfully`() =
        testApp({ eventWebApi(eventService, eventChangeStateService) }) {
            val organizer = createTestUser()
            val zone = createTestZone(reporterId = organizer.id)
            val event = createTestEvent(zoneId = zone.id, organizerId = organizer.id)
            val token = generateTestToken(organizer.id)

            val newTitle = "Updated Event Title"
            val newDescription = "Updated description."
            val newMaxParticipants = 100
            val newStartDate = futureStart.plus(1.days).toString()

            val requestBody =
                UpdateEventRequest(
                    title = newTitle,
                    description = newDescription,
                    startDate = newStartDate,
                    maxParticipants = newMaxParticipants,
                )

            val response =
                client.patch("/events/${event.id.value}") {
                    header(HttpHeaders.Authorization, "Bearer $token")
                    contentType(ContentType.Application.Json)
                    setBody(Json.encodeToString(requestBody))
                }
            assertEquals(HttpStatusCode.OK, response.status)
            val responseBody = Json.decodeFromString<EventResponse>(response.bodyAsText())

            assertEquals(newTitle, responseBody.title)
            assertEquals(newDescription, responseBody.description)
            assertEquals(newStartDate, responseBody.startDate)
            assertEquals(newMaxParticipants, responseBody.maxParticipants)

            val updatedEvent = fakeEventRepository.getById(event.id)
            assertNotNull(updatedEvent)
            assertEquals(newTitle, updatedEvent.title.value)
            assertEquals(newMaxParticipants, updatedEvent.maxParticipants)
        }

    @Test
    fun `PATCH event by ID - should fail with 400 for invalid date format in request`() =
        testApp({ eventWebApi(eventService, eventChangeStateService) }) {
            val organizer = createTestUser()
            val zone = createTestZone(reporterId = organizer.id)
            val event = createTestEvent(zoneId = zone.id, organizerId = organizer.id)
            val token = generateTestToken(organizer.id)
            val requestBody = UpdateEventRequest(startDate = "inv", endDate = futureEnd.toString())

            val response =
                client.patch("/events/${event.id.value}") {
                    header(HttpHeaders.Authorization, "Bearer $token")
                    contentType(ContentType.Application.Json)
                    setBody(Json.encodeToString(requestBody))
                }
            assertEquals(HttpStatusCode.BadRequest, response.status)
        }

    @Test
    fun `PATCH event by ID - should fail with 400 if new start date is in the past`() =
        testApp({ eventWebApi(eventService, eventChangeStateService) }) {
            val organizer = createTestUser()
            val zone = createTestZone(reporterId = organizer.id)
            val event = createTestEvent(zoneId = zone.id, organizerId = organizer.id)
            val token = generateTestToken(organizer.id)
            val past = now().minus(1.days).toString()
            val requestBody = UpdateEventRequest(startDate = past, endDate = futureEnd.toString())

            val response =
                client.patch("/events/${event.id.value}") {
                    header(HttpHeaders.Authorization, "Bearer $token")
                    contentType(ContentType.Application.Json)
                    setBody(Json.encodeToString(requestBody))
                }
            assertEquals(HttpStatusCode.BadRequest, response.status)
        }

    @Test
    fun `PATCH event by ID - should fail with 403 if not organizer`() =
        testApp({ eventWebApi(eventService, eventChangeStateService) }) {
            val organizer = createTestUser(name = Name("Org"), email = Email("org@test.com"))
            val otherUser = createTestUser(name = Name("Other"), email = Email("other@test.com"))
            val zone = createTestZone(reporterId = organizer.id)
            val event = createTestEvent(zoneId = zone.id, organizerId = organizer.id)
            val token = generateTestToken(otherUser.id)
            val requestBody = UpdateEventRequest(title = "Attempted Update")

            val response =
                client.patch("/events/${event.id.value}") {
                    header(HttpHeaders.Authorization, "Bearer $token")
                    contentType(ContentType.Application.Json)
                    setBody(Json.encodeToString(requestBody))
                }
            assertEquals(HttpStatusCode.Forbidden, response.status)
        }

    @Test
    fun `PATCH event by ID - should fail with 400 if maxParticipants less than participants`() =
        testApp({ eventWebApi(eventService, eventChangeStateService) }) {
            val organizer = createTestUser()
            val p1 = createTestUser(email = Email("p1@e.com"))
            val p2 = createTestUser(email = Email("p2@e.com"))
            val zone = createTestZone(reporterId = organizer.id)
            val event = createTestEvent(zone.id, organizer.id, setOf(p1.id, p2.id))
            val token = generateTestToken(organizer.id)

            val requestBody = UpdateEventRequest(maxParticipants = 1)

            val response =
                client.patch("/events/${event.id.value}") {
                    header(HttpHeaders.Authorization, "Bearer $token")
                    contentType(ContentType.Application.Json)
                    setBody(Json.encodeToString(requestBody))
                }
            assertEquals(HttpStatusCode.BadRequest, response.status)
        }

    @Test
    fun `PATCH event by ID - should fail with 409 if event is COMPLETED`() =
        testApp({ eventWebApi(eventService, eventChangeStateService) }) {
            val organizer = createTestUser()
            val zone = createTestZone(reporterId = organizer.id)
            val event = createTestEvent(zone.id, organizer.id, status = EventStatus.COMPLETED)
            val token = generateTestToken(organizer.id)
            val requestBody = UpdateEventRequest(title = "Too late update")

            val response =
                client.patch("/events/${event.id.value}") {
                    header(HttpHeaders.Authorization, "Bearer $token")
                    contentType(ContentType.Application.Json)
                    setBody(Json.encodeToString(requestBody))
                }
            assertEquals(HttpStatusCode.Conflict, response.status)
        }

    @Test
    fun `PATCH event by ID - should fail with 409 if event is CANCELLED`() =
        testApp({ eventWebApi(eventService, eventChangeStateService) }) {
            val organizer = createTestUser()
            val zone = createTestZone(reporterId = organizer.id)
            val event = createTestEvent(zone.id, organizer.id, status = EventStatus.CANCELLED)
            val token = generateTestToken(organizer.id)
            val requestBody = UpdateEventRequest(title = "Too late update")

            val response =
                client.patch("/events/${event.id.value}") {
                    header(HttpHeaders.Authorization, "Bearer $token")
                    contentType(ContentType.Application.Json)
                    setBody(Json.encodeToString(requestBody))
                }
            assertEquals(HttpStatusCode.Conflict, response.status)
        }

    @Test
    fun `DELETE event by ID - should delete successfully as organizer`() =
        testApp({ eventWebApi(eventService, eventChangeStateService) }) {
            val organizer = createTestUser()
            val zone = createTestZone(reporterId = organizer.id)
            val event = createTestEvent(zone.id, organizer.id, status = EventStatus.PLANNED)
            linkEventToZone(zone.id, event.id)
            val token = generateTestToken(organizer.id)

            assertNotNull(fakeEventRepository.getById(event.id))
            val initialZone = fakeZoneRepository.getById(zone.id)
            assertNotNull(initialZone)
            assertEquals(event.id, initialZone.eventId)
            assertEquals(ZoneStatus.CLEANING_SCHEDULED, initialZone.status)

            val response =
                client.delete("/events/${event.id.value}") {
                    header(HttpHeaders.Authorization, "Bearer $token")
                }
            assertEquals(HttpStatusCode.NoContent, response.status)
            assertNull(fakeEventRepository.getById(event.id))

            val updatedZone = fakeZoneRepository.getById(zone.id)
            assertNotNull(updatedZone)
            assertNull(updatedZone.eventId)
            assertEquals(ZoneStatus.REPORTED, updatedZone.status)
        }

    @Test
    fun `DELETE event by ID - should delete successfully as organizer for CANCELLED event`() =
        testApp({ eventWebApi(eventService, eventChangeStateService) }) {
            val organizer = createTestUser()
            val zone = createTestZone(reporterId = organizer.id)
            val event = createTestEvent(zone.id, organizer.id, status = EventStatus.CANCELLED)
            val token = generateTestToken(organizer.id)
            assertNotNull(fakeEventRepository.getById(event.id))

            val response =
                client.delete("/events/${event.id.value}") {
                    header(HttpHeaders.Authorization, "Bearer $token")
                }
            assertEquals(HttpStatusCode.NoContent, response.status)
            assertNull(fakeEventRepository.getById(event.id))
        }

    @Test
    fun `POST events - should fail with 401 Unauthorized if no token provided`() =
        testApp({ eventWebApi(eventService, eventChangeStateService) }) {
            val organizer = createTestUser(name = Name("Organizer User"))
            val zone = createTestZone(reporterId = organizer.id)

            val requestBody =
                CreateEventRequest(
                    title = "New Beach Cleanup",
                    description = "Let's clean the beach!",
                    startDate = futureStart.toString(),
                    endDate = futureEnd.toString(),
                    zoneId = zone.id.value,
                    maxParticipants = 50,
                )

            val response =
                client.post("/events") {
                    contentType(ContentType.Application.Json)
                    setBody(Json.encodeToString(requestBody))
                }
            assertEquals(HttpStatusCode.Unauthorized, response.status)
        }

    @Test
    fun `PATCH event by ID - should fail with 401 Unauthorized if no token provided`() =
        testApp({ eventWebApi(eventService, eventChangeStateService) }) {
            val organizer = createTestUser()
            val zone = createTestZone(reporterId = organizer.id)
            val event = createTestEvent(zoneId = zone.id, organizerId = organizer.id)

            val requestBody = UpdateEventRequest(title = "Attempted Update")

            val response =
                client.patch("/events/${event.id.value}") {
                    contentType(ContentType.Application.Json)
                    setBody(Json.encodeToString(requestBody))
                }
            assertEquals(HttpStatusCode.Unauthorized, response.status)
        }

    @Test
    fun `DELETE event by ID - should fail with 401 Unauthorized if no token provided`() =
        testApp({ eventWebApi(eventService, eventChangeStateService) }) {
            val organizer = createTestUser()
            val zone = createTestZone(reporterId = organizer.id)
            val event = createTestEvent(zoneId = zone.id, organizerId = organizer.id)

            val response = client.delete("/events/${event.id.value}")
            assertEquals(HttpStatusCode.Unauthorized, response.status)
        }

    @Test
    fun `DELETE event by ID - should fail with 403 if not organizer`() =
        testApp({ eventWebApi(eventService, eventChangeStateService) }) {
            val organizer = createTestUser(name = Name("Org"), email = Email("org@test.com"))
            val otherUser = createTestUser(name = Name("Other"), email = Email("other@test.com"))
            val zone = createTestZone(reporterId = organizer.id)
            val event = createTestEvent(zoneId = zone.id, organizerId = organizer.id)
            val token = generateTestToken(otherUser.id)

            val response =
                client.delete("/events/${event.id.value}") {
                    header(HttpHeaders.Authorization, "Bearer $token")
                }
            assertEquals(HttpStatusCode.Forbidden, response.status)
            assertNotNull(fakeEventRepository.getById(event.id))
        }

    @Test
    fun `DELETE event by ID - should fail with 409 if event status is IN_PROGRESS`() =
        testApp({ eventWebApi(eventService, eventChangeStateService) }) {
            val organizer = createTestUser()
            val zone = createTestZone(reporterId = organizer.id)
            val event = createTestEvent(zone.id, organizer.id, status = EventStatus.IN_PROGRESS)
            val token = generateTestToken(organizer.id)

            val response =
                client.delete("/events/${event.id.value}") {
                    header(HttpHeaders.Authorization, "Bearer $token")
                }
            assertEquals(HttpStatusCode.Conflict, response.status)
            assertNotNull(fakeEventRepository.getById(event.id))
        }

    @Test
    fun `DELETE event by ID - should fail with 409 if event status is COMPLETED`() =
        testApp({ eventWebApi(eventService, eventChangeStateService) }) {
            val organizer = createTestUser()
            val zone = createTestZone(reporterId = organizer.id)
            val event = createTestEvent(zone.id, organizer.id, status = EventStatus.COMPLETED)
            val token = generateTestToken(organizer.id)

            val response =
                client.delete("/events/${event.id.value}") {
                    header(HttpHeaders.Authorization, "Bearer $token")
                }
            assertEquals(HttpStatusCode.Conflict, response.status)
        }

    /*
    @Test
    fun `POST cancel event - should succeed as organizer`() =
        testApp({ eventWebApi(eventService, eventChangeStateService) }) {
            val organizer = createTestUser()
            val zone = createTestZone(reporterId = organizer.id)
            val event = createTestEvent(zone.id, organizer.id, status = EventStatus.PLANNED)
            linkEventToZone(zone.id, event.id)
            val token = generateTestToken(organizer.id)

            val response =
                client.post("/events/${event.id.value}/cancel") {
                    header(HttpHeaders.Authorization, "Bearer $token")
                }
            assertEquals(HttpStatusCode.OK, response.status)
            val responseBody = Json.decodeFromString<EventResponse>(response.bodyAsText())
            assertEquals(EventStatus.CANCELLED.name, responseBody.status)

            val cancelledEvent = fakeEventRepository.getById(event.id)
            assertNotNull(cancelledEvent)
            assertEquals(EventStatus.CANCELLED, cancelledEvent.status)

            val updatedZone = fakeZoneRepository.getById(zone.id)
            assertNotNull(updatedZone)
            assertNull(updatedZone.eventId)
            assertEquals(ZoneStatus.REPORTED, updatedZone.status)
        }

    @Test
    fun `POST cancel event - should succeed as organizer for IN_PROGRESS event`() =
        testApp({ eventWebApi(eventService, eventChangeStateService) }) {
            val organizer = createTestUser()
            val zone = createTestZone(reporterId = organizer.id)
            val event = createTestEvent(zone.id, organizer.id, status = EventStatus.IN_PROGRESS)
            val token = generateTestToken(organizer.id)

            val response =
                client.post("/events/${event.id.value}/cancel") {
                    header(HttpHeaders.Authorization, "Bearer $token")
                }
            assertEquals(HttpStatusCode.OK, response.status)
            val cancelledEvent = fakeEventRepository.getById(event.id)
            assertNotNull(cancelledEvent)
            assertEquals(EventStatus.CANCELLED, cancelledEvent.status)
        }

    @Test
    fun `POST cancel event - should fail with 403 if not organizer`() =
        testApp({ eventWebApi(eventService, eventChangeStateService) }) {
            val organizer = createTestUser(name = Name("Org"), email = Email("org@test.com"))
            val otherUser = createTestUser(name = Name("Other"), email = Email("other@test.com"))
            val zone = createTestZone(reporterId = organizer.id)
            val event = createTestEvent(zone.id, organizer.id, status = EventStatus.PLANNED)
            val token = generateTestToken(otherUser.id)

            val response =
                client.post("/events/${event.id.value}/cancel") {
                    header(HttpHeaders.Authorization, "Bearer $token")
                }
            assertEquals(HttpStatusCode.Forbidden, response.status)
        }

    @Test
    fun `POST cancel event - should fail with 409 if event already CANCELLED`() =
        testApp({ eventWebApi(eventService, eventChangeStateService) }) {
            val organizer = createTestUser()
            val zone = createTestZone(reporterId = organizer.id)
            val event = createTestEvent(zone.id, organizer.id, status = EventStatus.CANCELLED)
            val token = generateTestToken(organizer.id)

            val response =
                client.post("/events/${event.id.value}/cancel") {
                    header(HttpHeaders.Authorization, "Bearer $token")
                }
            assertEquals(HttpStatusCode.Conflict, response.status)
        }

    @Test
    fun `POST cancel event - should fail with 409 if event already COMPLETED`() =
        testApp({ eventWebApi(eventService, eventChangeStateService) }) {
            val organizer = createTestUser()
            val zone = createTestZone(reporterId = organizer.id)
            val event = createTestEvent(zone.id, organizer.id, status = EventStatus.COMPLETED)
            val token = generateTestToken(organizer.id)

            val response =
                client.post("/events/${event.id.value}/cancel") {
                    header(HttpHeaders.Authorization, "Bearer $token")
                }
            assertEquals(HttpStatusCode.Conflict, response.status)
        }

    @Test
    fun `POST complete event - should succeed as organizer for IN_PROGRESS event`() =
        testApp({ eventWebApi(eventService, eventChangeStateService) }) {
            val organizer = createTestUser()
            val zone = createTestZone(reporterId = organizer.id)
            val event =
                createTestEvent(
                    zoneId = zone.id,
                    organizerId = organizer.id,
                    status = EventStatus.IN_PROGRESS,
                    period = Period(now().minus(2.hours), now().minus(1.hours)),
                )
            linkEventToZone(zone.id, event.id)
            val token = generateTestToken(organizer.id)

            val response =
                client.post("/events/${event.id.value}/complete") {
                    header(HttpHeaders.Authorization, "Bearer $token")
                }
            assertEquals(HttpStatusCode.OK, response.status)
            val responseBody = Json.decodeFromString<EventResponse>(response.bodyAsText())
            assertEquals(EventStatus.COMPLETED.name, responseBody.status)

            val completedEvent = fakeEventRepository.getById(event.id)
            assertNotNull(completedEvent)
            assertEquals(EventStatus.COMPLETED, completedEvent.status)

            val updatedZone = fakeZoneRepository.getById(zone.id)
            assertNotNull(updatedZone)
            assertNull(updatedZone.eventId)
            assertEquals(ZoneStatus.CLEANED, updatedZone.status)
        }

    @Test
    fun `POST complete event - should succeed as organizer for PLANNED event if period has passed`() =
        testApp({ eventWebApi(eventService, eventChangeStateService) }) {
            val organizer = createTestUser()
            val zone = createTestZone(reporterId = organizer.id)
            val event =
                createTestEvent(
                    zoneId = zone.id,
                    organizerId = organizer.id,
                    status = EventStatus.PLANNED,
                    period = Period(now().minus(2.days).minus(2.hours), now().minus(1.days)),
                )
            val token = generateTestToken(organizer.id)

            val response =
                client.post("/events/${event.id.value}/complete") {
                    header(HttpHeaders.Authorization, "Bearer $token")
                }
            assertEquals(HttpStatusCode.OK, response.status)
            val completedEvent = fakeEventRepository.getById(event.id)
            assertNotNull(completedEvent)
            assertEquals(EventStatus.COMPLETED, completedEvent.status)
        }

    @Test
    fun `POST complete event - should fail with 403 if not organizer`() =
        testApp({ eventWebApi(eventService, eventChangeStateService) }) {
            val organizer = createTestUser(name = Name("Org"), email = Email("org@test.com"))
            val otherUser = createTestUser(name = Name("Other"), email = Email("other@test.com"))
            val zone = createTestZone(reporterId = organizer.id)
            val event = createTestEvent(zone.id, organizer.id, status = EventStatus.IN_PROGRESS)
            val token = generateTestToken(otherUser.id)

            val response =
                client.post("/events/${event.id.value}/complete") {
                    header(HttpHeaders.Authorization, "Bearer $token")
                }
            assertEquals(HttpStatusCode.Forbidden, response.status)
        }

    @Test
    fun `POST complete event - should fail with 409 if event already COMPLETED`() =
        testApp({ eventWebApi(eventService, eventChangeStateService) }) {
            val organizer = createTestUser()
            val zone = createTestZone(reporterId = organizer.id)
            val event = createTestEvent(zone.id, organizer.id, status = EventStatus.COMPLETED)
            val token = generateTestToken(organizer.id)

            val response =
                client.post("/events/${event.id.value}/complete") {
                    header(HttpHeaders.Authorization, "Bearer $token")
                }
            assertEquals(HttpStatusCode.Conflict, response.status)
        }

    @Test
    fun `POST complete event - should fail with 409 if event is CANCELLED`() =
        testApp({ eventWebApi(eventService, eventChangeStateService) }) {
            val organizer = createTestUser()
            val zone = createTestZone(reporterId = organizer.id)
            val event = createTestEvent(zone.id, organizer.id, status = EventStatus.CANCELLED)
            val token = generateTestToken(organizer.id)

            val response =
                client.post("/events/${event.id.value}/complete") {
                    header(HttpHeaders.Authorization, "Bearer $token")
                }
            assertEquals(HttpStatusCode.Conflict, response.status)
        }
     */

    @Test
    fun `POST join event - should succeed for PLANNED event`() =
        testApp({ eventWebApi(eventService, eventChangeStateService) }) {
            val organizer = createTestUser(name = Name("Org"), email = Email("org@test.com"))
            val participant = createTestUser(name = Name("Part"), email = Email("part@test.com"))
            val zone = createTestZone(reporterId = organizer.id)
            val event = createTestEvent(zone.id, organizer.id, emptySet(), EventStatus.PLANNED)
            val token = generateTestToken(participant.id)

            val response =
                client.post("/events/${event.id.value}/join") {
                    header(HttpHeaders.Authorization, "Bearer $token")
                }
            assertEquals(HttpStatusCode.OK, response.status)
            val responseBody = Json.decodeFromString<EventResponse>(response.bodyAsText())
            assertTrue(responseBody.participantsIds.contains(participant.id.value))
            assertEquals(1, responseBody.participantsIds.size)

            val updatedEvent = fakeEventRepository.getById(event.id)
            assertNotNull(updatedEvent)
            assertTrue(updatedEvent.participantsIds.contains(participant.id))
        }

    @Test
    fun `POST join event - should fail with 409 if event is full`() =
        testApp({ eventWebApi(eventService, eventChangeStateService) }) {
            val organizer = createTestUser(name = Name("Org"), email = Email("org@test.com"))
            val p1 = createTestUser(name = Name("P1"), email = Email("p1@test.com"))
            val p2Attempting = createTestUser(name = Name("P2"), email = Email("p2@test.com"))
            val zone = createTestZone(reporterId = organizer.id)
            val event =
                createTestEvent(
                    zoneId = zone.id,
                    organizerId = organizer.id,
                    participantsIds = setOf(p1.id),
                    maxParticipants = 1,
                    status = EventStatus.PLANNED,
                )
            val token = generateTestToken(p2Attempting.id)

            val response =
                client.post("/events/${event.id.value}/join") {
                    header(HttpHeaders.Authorization, "Bearer $token")
                }
            assertEquals(HttpStatusCode.Conflict, response.status)
        }

    @Test
    fun `POST join event - should fail with 409 if user already a participant`() =
        testApp({ eventWebApi(eventService, eventChangeStateService) }) {
            val organizer = createTestUser(name = Name("Org"), email = Email("org@test.com"))
            val participant = createTestUser(name = Name("Part"), email = Email("part@test.com"))
            val zone = createTestZone(reporterId = organizer.id)
            val event =
                createTestEvent(zone.id, organizer.id, setOf(participant.id), EventStatus.PLANNED)
            val token = generateTestToken(participant.id)

            val response =
                client.post("/events/${event.id.value}/join") {
                    header(HttpHeaders.Authorization, "Bearer $token")
                }
            assertEquals(HttpStatusCode.Conflict, response.status)
        }

    /*
    @Test
    fun `POST join event - should fail with 409 if event is IN_PROGRESS`() =
        testApp({ eventWebApi(eventService, eventChangeStateService) }) {
            val organizer = createTestUser(name = Name("Org"), email = Email("org@test.com"))
            val user = createTestUser(name = Name("User"), email = Email("user@test.com"))
            val zone = createTestZone(reporterId = organizer.id)
            val event = createTestEvent(zone.id, organizer.id, status = EventStatus.IN_PROGRESS)
            val token = generateTestToken(user.id)

            val response =
                client.post("/events/${event.id.value}/join") {
                    header(HttpHeaders.Authorization, "Bearer $token")
                }
            assertEquals(HttpStatusCode.Conflict, response.status)
        }
     */

    @Test
    fun `POST join event - should fail with 409 if event is CANCELLED`() =
        testApp({ eventWebApi(eventService, eventChangeStateService) }) {
            val organizer = createTestUser(name = Name("Org"), email = Email("org@test.com"))
            val user = createTestUser(name = Name("User"), email = Email("user@test.com"))
            val zone = createTestZone(reporterId = organizer.id)
            val event = createTestEvent(zone.id, organizer.id, status = EventStatus.CANCELLED)
            val token = generateTestToken(user.id)

            val response =
                client.post("/events/${event.id.value}/join") {
                    header(HttpHeaders.Authorization, "Bearer $token")
                }
            assertEquals(HttpStatusCode.Conflict, response.status)
        }

    @Test
    fun `POST join event - should fail with 409 if event is COMPLETED`() =
        testApp({ eventWebApi(eventService, eventChangeStateService) }) {
            val organizer = createTestUser(name = Name("Org"), email = Email("org@test.com"))
            val user = createTestUser(name = Name("User"), email = Email("user@test.com"))
            val zone = createTestZone(reporterId = organizer.id)
            val event = createTestEvent(zone.id, organizer.id, status = EventStatus.COMPLETED)
            val token = generateTestToken(user.id)

            val response =
                client.post("/events/${event.id.value}/join") {
                    header(HttpHeaders.Authorization, "Bearer $token")
                }
            assertEquals(HttpStatusCode.Conflict, response.status)
        }

    /*
    @Test
    fun `POST join event - should fail with 409 if user is the organizer`() =
        testApp({ eventWebApi(eventService, eventChangeStateService) }) {
            val organizer = createTestUser()
            val zone = createTestZone(reporterId = organizer.id)
            val event = createTestEvent(zone.id, organizer.id, status = EventStatus.PLANNED)
            val token = generateTestToken(organizer.id)

            val response =
                client.post("/events/${event.id.value}/join") {
                    header(HttpHeaders.Authorization, "Bearer $token")
                }
            assertEquals(HttpStatusCode.Conflict, response.status)
        }
     */

    @Test
    fun `POST leave event - should succeed if participant`() =
        testApp({ eventWebApi(eventService, eventChangeStateService) }) {
            val organizer = createTestUser(name = Name("Org"), email = Email("org@test.com"))
            val participant = createTestUser(name = Name("Part"), email = Email("part@test.com"))
            val zone = createTestZone(reporterId = organizer.id)
            val event =
                createTestEvent(zone.id, organizer.id, setOf(participant.id), EventStatus.PLANNED)
            val token = generateTestToken(participant.id)

            val response =
                client.post("/events/${event.id.value}/leave") {
                    header(HttpHeaders.Authorization, "Bearer $token")
                }
            assertEquals(HttpStatusCode.OK, response.status)
            val responseBody = Json.decodeFromString<EventResponse>(response.bodyAsText())
            assertFalse(responseBody.participantsIds.contains(participant.id.value))
            assertEquals(0, responseBody.participantsIds.size)

            val updatedEvent = fakeEventRepository.getById(event.id)
            assertNotNull(updatedEvent)
            assertFalse(updatedEvent.participantsIds.contains(participant.id))
        }

    @Test
    fun `POST leave event - should fail with 404 if user not a participant`() =
        testApp({ eventWebApi(eventService, eventChangeStateService) }) {
            val organizer = createTestUser(name = Name("Org"), email = Email("org@test.com"))
            val nonParticipant = createTestUser(name = Name("Non"), email = Email("non@test.com"))
            val zone = createTestZone(reporterId = organizer.id)
            val event = createTestEvent(zone.id, organizer.id, emptySet(), EventStatus.PLANNED)
            val token = generateTestToken(nonParticipant.id)

            val response =
                client.post("/events/${event.id.value}/leave") {
                    header(HttpHeaders.Authorization, "Bearer $token")
                }
            assertEquals(HttpStatusCode.NotFound, response.status)
        }

    @Test
    fun `POST leave event - should fail with 409 if event is IN_PROGRESS`() =
        testApp({ eventWebApi(eventService, eventChangeStateService) }) {
            val organizer = createTestUser(name = Name("Org"), email = Email("org@test.com"))
            val participant = createTestUser(name = Name("Part"), email = Email("part@test.com"))
            val zone = createTestZone(reporterId = organizer.id)
            val event =
                createTestEvent(
                    zoneId = zone.id,
                    organizerId = organizer.id,
                    participantsIds = setOf(participant.id),
                    status = EventStatus.IN_PROGRESS,
                )
            val token = generateTestToken(participant.id)

            val response =
                client.post("/events/${event.id.value}/leave") {
                    header(HttpHeaders.Authorization, "Bearer $token")
                }
            assertEquals(HttpStatusCode.Conflict, response.status)
        }

    @Test
    fun `POST leave event - should fail with 409 if event is CANCELLED`() =
        testApp({ eventWebApi(eventService, eventChangeStateService) }) {
            val organizer = createTestUser(name = Name("Org"), email = Email("org@test.com"))
            val participant = createTestUser(name = Name("Part"), email = Email("part@test.com"))
            val zone = createTestZone(reporterId = organizer.id)
            val event =
                createTestEvent(
                    zoneId = zone.id,
                    organizerId = organizer.id,
                    participantsIds = setOf(participant.id),
                    status = EventStatus.CANCELLED,
                )
            val token = generateTestToken(participant.id)

            val response =
                client.post("/events/${event.id.value}/leave") {
                    header(HttpHeaders.Authorization, "Bearer $token")
                }
            assertEquals(HttpStatusCode.Conflict, response.status)
        }

    @Test
    fun `POST leave event - should fail with 409 if event is COMPLETED`() =
        testApp({ eventWebApi(eventService, eventChangeStateService) }) {
            val organizer = createTestUser(name = Name("Org"), email = Email("org@test.com"))
            val participant = createTestUser(name = Name("Part"), email = Email("part@test.com"))
            val zone = createTestZone(reporterId = organizer.id)
            val event =
                createTestEvent(
                    zoneId = zone.id,
                    organizerId = organizer.id,
                    participantsIds = setOf(participant.id),
                    status = EventStatus.COMPLETED,
                )
            val token = generateTestToken(participant.id)

            val response =
                client.post("/events/${event.id.value}/leave") {
                    header(HttpHeaders.Authorization, "Bearer $token")
                }
            assertEquals(HttpStatusCode.Conflict, response.status)
        }

    @Test
    fun `GET event participants - should return list of participants`() =
        testApp({ eventWebApi(eventService, eventChangeStateService) }) {
            val organizer = createTestUser(name = Name("Org"), email = Email("org@test.com"))
            val p1 = createTestUser(name = Name("P1"), email = Email("p1@test.com"))
            val p2 = createTestUser(name = Name("P2"), email = Email("p2@test.com"))
            val zone = createTestZone(reporterId = organizer.id)
            val event = createTestEvent(zone.id, organizer.id, setOf(p1.id, p2.id))

            val response = client.get("/events/${event.id.value}/participants")
            assertEquals(HttpStatusCode.OK, response.status)
            val responseList = Json.decodeFromString<List<UserResponse>>(response.bodyAsText())
            assertEquals(2, responseList.size)
            assertTrue(responseList.any { it.id == p1.id.value && it.name == p1.name.value })
            assertTrue(responseList.any { it.id == p2.id.value && it.name == p2.name.value })
        }

    @Test
    fun `GET event participants - should return empty list if no participants`() =
        testApp({ eventWebApi(eventService, eventChangeStateService) }) {
            val organizer = createTestUser()
            val zone = createTestZone(reporterId = organizer.id)
            val event = createTestEvent(zone.id, organizer.id, emptySet())

            val response = client.get("/events/${event.id.value}/participants")
            assertEquals(HttpStatusCode.OK, response.status)
            assertEquals("[]", response.bodyAsText())
        }

    /*
    @Test
    fun `POST cancel event - should fail with 401 Unauthorized if no token`() =
        testApp({ eventWebApi(eventService, eventChangeStateService) }) {
            val organizer = createTestUser()
            val zone = createTestZone(reporterId = organizer.id)
            val event = createTestEvent(zoneId = zone.id, organizerId = organizer.id)

            val response = client.post("/events/${event.id.value}/cancel")
            assertEquals(HttpStatusCode.Unauthorized, response.status)
        }

    @Test
    fun `POST complete event - should fail with 401 Unauthorized if no token`() =
        testApp({ eventWebApi(eventService, eventChangeStateService) }) {
            val organizer = createTestUser()
            val zone = createTestZone(reporterId = organizer.id)
            val event = createTestEvent(zoneId = zone.id, organizerId = organizer.id)

            val response = client.post("/events/${event.id.value}/complete")
            assertEquals(HttpStatusCode.Unauthorized, response.status)
        }
     */

    @Test
    fun `POST join event - should fail with 401 Unauthorized if no token`() =
        testApp({ eventWebApi(eventService, eventChangeStateService) }) {
            val organizer = createTestUser()
            val zone = createTestZone(reporterId = organizer.id)
            val event = createTestEvent(zoneId = zone.id, organizerId = organizer.id)

            val response = client.post("/events/${event.id.value}/join")
            assertEquals(HttpStatusCode.Unauthorized, response.status)
        }

    @Test
    fun `POST leave event - should fail with 401 Unauthorized if no token`() =
        testApp({ eventWebApi(eventService, eventChangeStateService) }) {
            val organizer = createTestUser()
            val zone = createTestZone(reporterId = organizer.id)
            val event = createTestEvent(zoneId = zone.id, organizerId = organizer.id)

            val response = client.post("/events/${event.id.value}/leave")
            assertEquals(HttpStatusCode.Unauthorized, response.status)
        }
}
