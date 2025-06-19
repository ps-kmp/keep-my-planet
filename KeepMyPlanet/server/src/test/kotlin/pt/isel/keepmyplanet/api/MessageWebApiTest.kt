package pt.isel.keepmyplanet.api

import io.ktor.client.request.delete
import io.ktor.client.request.get
import io.ktor.client.request.header
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.client.statement.bodyAsText
import io.ktor.http.ContentType
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpStatusCode
import io.ktor.http.contentType
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlinx.serialization.json.Json
import pt.isel.keepmyplanet.domain.common.Id
import pt.isel.keepmyplanet.domain.event.EventStatus
import pt.isel.keepmyplanet.domain.user.Email
import pt.isel.keepmyplanet.dto.message.CreateMessageRequest
import pt.isel.keepmyplanet.dto.message.MessageResponse
import pt.isel.keepmyplanet.service.MessageService

class MessageWebApiTest : BaseWebApiTest() {
    private val messageService =
        MessageService(
            messageRepository = fakeMessageRepository,
            eventRepository = fakeEventRepository,
            userRepository = fakeUserRepository,
            chatSseService = chatSseService,
        )

    @Test
    fun `POST message - should create message successfully as organizer`() =
        testApp({ messageWebApi(messageService, chatSseService) }) {
            val organizer = createTestUser()
            val zone = createTestZone()
            val event = createTestEvent(zoneId = zone.id, organizerId = organizer.id)
            val requestBody = CreateMessageRequest(content = "Hello from organizer!")
            val token = generateTestToken(organizer.id)

            val response =
                client.post("/events/${event.id.value}/chat") {
                    header(HttpHeaders.Authorization, "Bearer $token")
                    contentType(ContentType.Application.Json)
                    setBody(Json.encodeToString(requestBody))
                }
            assertEquals(HttpStatusCode.Created, response.status)

            val responseBody = Json.decodeFromString<MessageResponse>(response.bodyAsText())
            assertEquals(event.id.value, responseBody.eventId)
            assertEquals(organizer.id.value, responseBody.senderId)
            assertEquals(organizer.name.value, responseBody.senderName)
            assertEquals(requestBody.content, responseBody.content)
            assertEquals(0, responseBody.chatPosition, "First message should have chatPosition 0")

            val createdMessage = fakeMessageRepository.getById(Id(responseBody.id))
            assertNotNull(createdMessage, "Message should exist in repository")
            assertEquals(event.id, createdMessage.eventId)
            assertEquals(organizer.id, createdMessage.senderId)
            assertEquals(requestBody.content, createdMessage.content.value)
            assertEquals(0, createdMessage.chatPosition)
        }

    @Test
    fun `POST message - should create message successfully as participant`() =
        testApp({ messageWebApi(messageService, chatSseService) }) {
            val organizer = createTestUser()
            val participant = createTestUser(email = Email("part@test.com"))
            val zone = createTestZone()
            val event = createTestEvent(zone.id, organizer.id, setOf(participant.id))
            val requestBody = CreateMessageRequest(content = "Hi from participant!")
            val token = generateTestToken(participant.id)

            val response =
                client.post("/events/${event.id.value}/chat") {
                    header(HttpHeaders.Authorization, "Bearer $token")
                    contentType(ContentType.Application.Json)
                    setBody(Json.encodeToString(requestBody))
                }
            assertEquals(HttpStatusCode.Created, response.status)

            val responseBody = Json.decodeFromString<MessageResponse>(response.bodyAsText())
            assertEquals(event.id.value, responseBody.eventId)
            assertEquals(participant.id.value, responseBody.senderId)
            assertEquals(participant.name.value, responseBody.senderName)
            assertEquals(requestBody.content, responseBody.content)
            assertEquals(0, responseBody.chatPosition)

            val createdMessage = fakeMessageRepository.getById(Id(responseBody.id))
            assertNotNull(createdMessage)
            assertEquals(participant.id, createdMessage.senderId)
            assertEquals(0, createdMessage.chatPosition)
        }

    @Test
    fun `POST message - should assign incremental sequence numbers (chatPosition)`() =
        testApp({ messageWebApi(messageService, chatSseService) }) {
            val user1 = createTestUser()
            val user2 = createTestUser(email = Email("u2@test.com"))
            val zone = createTestZone()
            val event = createTestEvent(zone.id, user1.id, setOf(user2.id))
            val requestBody1 = CreateMessageRequest(content = "First")
            val requestBody2 = CreateMessageRequest(content = "Second")
            val tokenUser1 = generateTestToken(user1.id)
            val tokenUser2 = generateTestToken(user2.id)

            val response1 =
                client.post("/events/${event.id.value}/chat") {
                    header(HttpHeaders.Authorization, "Bearer $tokenUser1")
                    contentType(ContentType.Application.Json)
                    setBody(Json.encodeToString(requestBody1))
                }
            assertEquals(HttpStatusCode.Created, response1.status)
            val responseBody1 = Json.decodeFromString<MessageResponse>(response1.bodyAsText())
            assertEquals(0, responseBody1.chatPosition)

            val response2 =
                client.post("/events/${event.id.value}/chat") {
                    header(HttpHeaders.Authorization, "Bearer $tokenUser2")
                    contentType(ContentType.Application.Json)
                    setBody(Json.encodeToString(requestBody2))
                }
            assertEquals(HttpStatusCode.Created, response2.status)
            val responseBody2 = Json.decodeFromString<MessageResponse>(response2.bodyAsText())
            assertEquals(1, responseBody2.chatPosition)

            val messages =
                fakeMessageRepository.getAllByEventId(event.id).sortedBy { it.chatPosition }
            assertEquals(2, messages.size)
            assertEquals(0, messages[0].chatPosition)
            assertEquals(requestBody1.content, messages[0].content.value)
            assertEquals(user1.id, messages[0].senderId)
            assertEquals(1, messages[1].chatPosition)
            assertEquals(requestBody2.content, messages[1].content.value)
            assertEquals(user2.id, messages[1].senderId)
        }

    @Test
    fun `POST message - should fail with 404 if event not found`() =
        testApp({ messageWebApi(messageService, chatSseService) }) {
            val user = createTestUser()
            val nonExistentEventId = 999u
            val requestBody = CreateMessageRequest(content = "Doesn't matter")
            val token = generateTestToken(user.id)

            val response =
                client.post("/events/$nonExistentEventId/chat") {
                    header(HttpHeaders.Authorization, "Bearer $token")
                    contentType(ContentType.Application.Json)
                    setBody(Json.encodeToString(requestBody))
                }
            assertEquals(HttpStatusCode.NotFound, response.status)
        }

    @Test
    fun `POST message - should fail with 403 if sender is not organizer or participant`() =
        testApp({ messageWebApi(messageService, chatSseService) }) {
            val organizer = createTestUser()
            val participant = createTestUser(email = Email("part@test.com"))
            val nonParticipant = createTestUser(email = Email("outsider@test.com"))
            val zone = createTestZone()
            val event = createTestEvent(zone.id, organizer.id, setOf(participant.id))
            val requestBody = CreateMessageRequest(content = "Intruder message")
            val tokenNonParticipant = generateTestToken(nonParticipant.id)

            val response =
                client.post("/events/${event.id.value}/chat") {
                    header(HttpHeaders.Authorization, "Bearer $tokenNonParticipant")
                    contentType(ContentType.Application.Json)
                    setBody(Json.encodeToString(requestBody))
                }
            assertEquals(HttpStatusCode.Forbidden, response.status)
        }

    @Test
    fun `POST message - should fail with 401 if no auth token`() =
        testApp({ messageWebApi(messageService, chatSseService) }) {
            val organizer = createTestUser()
            val zone = createTestZone()
            val event = createTestEvent(zone.id, organizer.id)
            val requestBody = CreateMessageRequest(content = "Test message")

            val response =
                client.post("/events/${event.id.value}/chat") {
                    // No Authorization header
                    contentType(ContentType.Application.Json)
                    setBody(Json.encodeToString(requestBody))
                }
            assertEquals(HttpStatusCode.Unauthorized, response.status)
        }

    @Test
    fun `POST message - should fail with 409 if event is COMPLETED`() =
        testApp({ messageWebApi(messageService, chatSseService) }) {
            val user = createTestUser()
            val zone = createTestZone()
            val event = createTestEvent(zone.id, user.id, status = EventStatus.COMPLETED)
            val requestBody = CreateMessageRequest(content = "Too late")
            val token = generateTestToken(user.id)

            val response =
                client.post("/events/${event.id.value}/chat") {
                    header(HttpHeaders.Authorization, "Bearer $token")
                    contentType(ContentType.Application.Json)
                    setBody(Json.encodeToString(requestBody))
                }
            assertEquals(HttpStatusCode.Conflict, response.status)
        }

    @Test
    fun `POST message - should fail with 409 if event is CANCELLED`() =
        testApp({ messageWebApi(messageService, chatSseService) }) {
            val user = createTestUser()
            val zone = createTestZone()
            val event = createTestEvent(zone.id, user.id, status = EventStatus.CANCELLED)
            val requestBody = CreateMessageRequest(content = "Event cancelled")
            val token = generateTestToken(user.id)

            val response =
                client.post("/events/${event.id.value}/chat") {
                    header(HttpHeaders.Authorization, "Bearer $token")
                    contentType(ContentType.Application.Json)
                    setBody(Json.encodeToString(requestBody))
                }
            assertEquals(HttpStatusCode.Conflict, response.status)
        }

    @Test
    fun `POST message - should fail with 400 for invalid event ID format`() =
        testApp({ messageWebApi(messageService, chatSseService) }) {
            val user = createTestUser()
            val requestBody = CreateMessageRequest(content = "Valid content")
            val token = generateTestToken(user.id)

            val response =
                client.post("/events/abc/chat") {
                    header(HttpHeaders.Authorization, "Bearer $token")
                    contentType(ContentType.Application.Json)
                    setBody(Json.encodeToString(requestBody))
                }
            assertEquals(HttpStatusCode.BadRequest, response.status)
        }

    @Test
    fun `POST message - should fail with 400 for blank content`() =
        testApp({ messageWebApi(messageService, chatSseService) }) {
            val user = createTestUser()
            val zone = createTestZone()
            val event = createTestEvent(zoneId = zone.id, organizerId = user.id)
            val requestBody = CreateMessageRequest(content = "")
            val token = generateTestToken(user.id)

            val response =
                client.post("/events/${event.id.value}/chat") {
                    header(HttpHeaders.Authorization, "Bearer $token")
                    contentType(ContentType.Application.Json)
                    setBody(Json.encodeToString(requestBody))
                }
            assertEquals(HttpStatusCode.BadRequest, response.status)
        }

    @Test
    fun `GET messages - should return empty list for new event`() =
        testApp({ messageWebApi(messageService, chatSseService) }) {
            val user = createTestUser() // User for token
            val zone = createTestZone()
            val event = createTestEvent(zoneId = zone.id, organizerId = user.id)
            val token = generateTestToken(user.id)

            val response =
                client.get("/events/${event.id.value}/chat") {
                    header(HttpHeaders.Authorization, "Bearer $token")
                }
            assertEquals(HttpStatusCode.OK, response.status)
            assertEquals("[]", response.bodyAsText())
        }

    @Test
    fun `GET messages - should return list of messages sorted by sequence number`() =
        testApp({ messageWebApi(messageService, chatSseService) }) {
            val user1 = createTestUser()
            val user2 = createTestUser(email = Email("p1@test.com"))
            val zone = createTestZone()
            val event = createTestEvent(zone.id, user1.id, setOf(user2.id))
            val token = generateTestToken(user1.id)

            val msg1 = createTestMessage(event.id, user1.id, user1.name, "First")
            val msg2 = createTestMessage(event.id, user2.id, user2.name, "Second")
            val msg3 = createTestMessage(event.id, user1.id, user1.name, "Third")

            val response =
                client.get("/events/${event.id.value}/chat") {
                    header(HttpHeaders.Authorization, "Bearer $token")
                }
            assertEquals(HttpStatusCode.OK, response.status)

            val responseList = Json.decodeFromString<List<MessageResponse>>(response.bodyAsText())
            assertEquals(3, responseList.size)

            assertEquals(msg1.id.value, responseList[0].id)
            assertEquals(0, responseList[0].chatPosition)
            assertEquals(msg1.content.value, responseList[0].content)
            assertEquals(user1.name.value, responseList[0].senderName)

            assertEquals(msg2.id.value, responseList[1].id)
            assertEquals(1, responseList[1].chatPosition)
            assertEquals(msg2.content.value, responseList[1].content)
            assertEquals(user2.name.value, responseList[1].senderName)

            assertEquals(msg3.id.value, responseList[2].id)
            assertEquals(2, responseList[2].chatPosition)
            assertEquals(msg3.content.value, responseList[2].content)
            assertEquals(user1.name.value, responseList[2].senderName)
        }

    @Test
    fun `GET messages - should fail with 404 if event not found`() =
        testApp({ messageWebApi(messageService, chatSseService) }) {
            val user = createTestUser()
            val nonExistentEventId = 999u
            val token = generateTestToken(user.id)

            val response =
                client.get("/events/$nonExistentEventId/chat") {
                    header(HttpHeaders.Authorization, "Bearer $token")
                }
            assertEquals(HttpStatusCode.NotFound, response.status)
        }

    @Test
    fun `GET messages - should return 400 for invalid event ID`() =
        testApp({ messageWebApi(messageService, chatSseService) }) {
            val user = createTestUser()
            val token = generateTestToken(user.id)
            val response =
                client.get("/events/abc/chat") {
                    header(HttpHeaders.Authorization, "Bearer $token")
                }
            assertEquals(HttpStatusCode.BadRequest, response.status)
        }

    @Test
    fun `GET messages - should fail with 401 if no auth token`() =
        testApp({ messageWebApi(messageService, chatSseService) }) {
            val organizer = createTestUser()
            val zone = createTestZone()
            val event = createTestEvent(zone.id, organizer.id)

            val response =
                client.get("/events/${event.id.value}/chat") {
                    // No Authorization header
                }
            assertEquals(HttpStatusCode.Unauthorized, response.status)
        }

    @Test
    fun `GET single message - should return message by sequence number`() =
        testApp({ messageWebApi(messageService, chatSseService) }) {
            val user = createTestUser()
            val zone = createTestZone()
            val event = createTestEvent(zoneId = zone.id, organizerId = user.id)
            val token = generateTestToken(user.id)

            createTestMessage(event.id, user.id, user.name, "Message Zero")
            val msg1 = createTestMessage(event.id, user.id, user.name, "Message One")

            val response =
                client.get("/events/${event.id.value}/chat/${msg1.chatPosition}") {
                    header(HttpHeaders.Authorization, "Bearer $token")
                }
            assertEquals(HttpStatusCode.OK, response.status)

            val responseBody = Json.decodeFromString<MessageResponse>(response.bodyAsText())
            assertEquals(msg1.id.value, responseBody.id)
            assertEquals(msg1.content.value, responseBody.content)
            assertEquals(msg1.senderId.value, responseBody.senderId)
            assertEquals(msg1.senderName.value, responseBody.senderName)
            assertEquals(1, responseBody.chatPosition)
        }

    @Test
    fun `GET single message - should fail with 404 if event not found`() =
        testApp({ messageWebApi(messageService, chatSseService) }) {
            val user = createTestUser()
            val nonExistentEventId = 999u
            val token = generateTestToken(user.id)
            val response =
                client.get("/events/$nonExistentEventId/chat/0") {
                    header(HttpHeaders.Authorization, "Bearer $token")
                }
            assertEquals(HttpStatusCode.NotFound, response.status)
        }

    @Test
    fun `GET single message - should fail with 404 if sequence number not found`() =
        testApp({ messageWebApi(messageService, chatSseService) }) {
            val user = createTestUser()
            val zone = createTestZone()
            val event = createTestEvent(zoneId = zone.id, organizerId = user.id)
            val token = generateTestToken(user.id)
            createTestMessage(event.id, user.id, user.name)

            val response =
                client.get("/events/${event.id.value}/chat/5") {
                    // Sequence 5 likely not found
                    header(HttpHeaders.Authorization, "Bearer $token")
                }
            assertEquals(HttpStatusCode.NotFound, response.status)
        }

    @Test
    fun `GET single message - should fail with 400 for invalid event ID format`() =
        testApp({ messageWebApi(messageService, chatSseService) }) {
            val user = createTestUser()
            val token = generateTestToken(user.id)
            val response =
                client.get("/events/bad-id/chat/0") {
                    header(HttpHeaders.Authorization, "Bearer $token")
                }
            assertEquals(HttpStatusCode.BadRequest, response.status)
        }

    @Test
    fun `GET single message - should fail with 400 for invalid sequence number format`() =
        testApp({ messageWebApi(messageService, chatSseService) }) {
            val user = createTestUser()
            val zone = createTestZone()
            val event = createTestEvent(zoneId = zone.id, organizerId = user.id)
            val token = generateTestToken(user.id)

            val response =
                client.get("/events/${event.id.value}/chat/abc") {
                    header(HttpHeaders.Authorization, "Bearer $token")
                }
            assertEquals(HttpStatusCode.BadRequest, response.status)
        }

    @Test
    fun `GET single message - should fail with 400 for negative sequence number`() =
        testApp({ messageWebApi(messageService, chatSseService) }) {
            val user = createTestUser()
            val zone = createTestZone()
            val event = createTestEvent(zoneId = zone.id, organizerId = user.id)
            val token = generateTestToken(user.id)

            val response =
                client.get("/events/${event.id.value}/chat/-1") {
                    header(HttpHeaders.Authorization, "Bearer $token")
                }
            assertEquals(HttpStatusCode.BadRequest, response.status)
        }

    @Test
    fun `GET single message - should fail with 401 if no auth token`() =
        testApp({ messageWebApi(messageService, chatSseService) }) {
            val user = createTestUser()
            val zone = createTestZone()
            val event = createTestEvent(zoneId = zone.id, organizerId = user.id)
            val msg = createTestMessage(event.id, user.id, user.name)

            val response =
                client.get("/events/${event.id.value}/chat/${msg.chatPosition}") {
                    // No Authorization header
                }
            assertEquals(HttpStatusCode.Unauthorized, response.status)
        }

    @Test
    fun `DELETE message - should succeed when sender deletes own message`() =
        testApp({ messageWebApi(messageService, chatSseService) }) {
            val participant = createTestUser()
            val zone = createTestZone()
            val organizerPlaceholder = createTestUser(email = Email("org@delete.com"))
            val event = createTestEvent(zone.id, organizerPlaceholder.id, setOf(participant.id))
            val msg = createTestMessage(event.id, participant.id, participant.name)
            val token = generateTestToken(participant.id)
            assertNotNull(
                fakeMessageRepository.getSingleByEventIdAndSeqNum(event.id, msg.chatPosition),
            )

            val response =
                client.delete("/events/${event.id.value}/chat/${msg.chatPosition}") {
                    header(HttpHeaders.Authorization, "Bearer $token")
                }
            assertEquals(HttpStatusCode.NoContent, response.status)
            assertNull(
                fakeMessageRepository.getSingleByEventIdAndSeqNum(event.id, msg.chatPosition),
            )
        }

    @Test
    fun `DELETE message - should succeed when organizer deletes any message`() =
        testApp({ messageWebApi(messageService, chatSseService) }) {
            val organizer = createTestUser()
            val participant = createTestUser(email = Email("p1@test.com"))
            val zone = createTestZone()
            val event = createTestEvent(zone.id, organizer.id, setOf(participant.id))
            val msg = createTestMessage(event.id, participant.id, participant.name)
            val tokenOrganizer = generateTestToken(organizer.id)
            assertNotNull(
                fakeMessageRepository.getSingleByEventIdAndSeqNum(event.id, msg.chatPosition),
            )

            val response =
                client.delete("/events/${event.id.value}/chat/${msg.chatPosition}") {
                    header(HttpHeaders.Authorization, "Bearer $tokenOrganizer")
                }
            assertEquals(HttpStatusCode.NoContent, response.status)
            assertNull(
                fakeMessageRepository.getSingleByEventIdAndSeqNum(event.id, msg.chatPosition),
            )
        }

    @Test
    fun `DELETE message - should fail with 403 when non-sender, non-organizer tries to delete`() =
        testApp({ messageWebApi(messageService, chatSseService) }) {
            val organizer = createTestUser()
            val participant = createTestUser(email = Email("p1@test.com"))
            val outsider = createTestUser(email = Email("out@test.com"))
            val zone = createTestZone()
            val event = createTestEvent(zone.id, organizer.id, setOf(participant.id))
            val msg = createTestMessage(event.id, participant.id, participant.name)
            val tokenOutsider = generateTestToken(outsider.id)
            assertNotNull(
                fakeMessageRepository.getSingleByEventIdAndSeqNum(event.id, msg.chatPosition),
            )

            val response =
                client.delete("/events/${event.id.value}/chat/${msg.chatPosition}") {
                    header(HttpHeaders.Authorization, "Bearer $tokenOutsider")
                }
            assertEquals(HttpStatusCode.Forbidden, response.status)
            assertNotNull(
                fakeMessageRepository.getSingleByEventIdAndSeqNum(event.id, msg.chatPosition),
            )
        }

    @Test
    fun `DELETE message - should fail with 401 if no auth token`() =
        testApp({ messageWebApi(messageService, chatSseService) }) {
            val participant = createTestUser()
            val zone = createTestZone()
            val organizerPlaceholder = createTestUser(email = Email("org@delete.com"))
            val event = createTestEvent(zone.id, organizerPlaceholder.id, setOf(participant.id))
            val msg = createTestMessage(event.id, participant.id, participant.name)

            val response =
                client.delete("/events/${event.id.value}/chat/${msg.chatPosition}") {
                    // No Authorization header
                }
            assertEquals(HttpStatusCode.Unauthorized, response.status)
            assertNotNull(
                fakeMessageRepository.getSingleByEventIdAndSeqNum(event.id, msg.chatPosition),
            )
        }

    @Test
    fun `DELETE message - should fail with 404 if event not found`() =
        testApp({ messageWebApi(messageService, chatSseService) }) {
            val user = createTestUser()
            val nonExistentEventId = 999u
            val token = generateTestToken(user.id)

            val response =
                client.delete("/events/$nonExistentEventId/chat/0") {
                    header(HttpHeaders.Authorization, "Bearer $token")
                }
            assertEquals(HttpStatusCode.NotFound, response.status)
        }

    @Test
    fun `DELETE message - should fail with 404 if sequence number not found`() =
        testApp({ messageWebApi(messageService, chatSseService) }) {
            val user = createTestUser()
            val zone = createTestZone()
            val event = createTestEvent(zoneId = zone.id, organizerId = user.id)
            val msg = createTestMessage(event.id, user.id, user.name)
            val token = generateTestToken(user.id)
            assertNotNull(
                fakeMessageRepository.getSingleByEventIdAndSeqNum(event.id, msg.chatPosition),
            )

            val response =
                client.delete("/events/${event.id.value}/chat/5") {
                    // Sequence 5 likely not found
                    header(HttpHeaders.Authorization, "Bearer $token")
                }
            assertEquals(HttpStatusCode.NotFound, response.status)
            assertNotNull(
                fakeMessageRepository.getSingleByEventIdAndSeqNum(event.id, msg.chatPosition),
            )
        }

    @Test
    fun `DELETE message - should fail with 400 for invalid event ID format`() =
        testApp({ messageWebApi(messageService, chatSseService) }) {
            val user = createTestUser()
            val token = generateTestToken(user.id)
            val response =
                client.delete("/events/bad-id/chat/0") {
                    header(HttpHeaders.Authorization, "Bearer $token")
                }
            assertEquals(HttpStatusCode.BadRequest, response.status)
        }

    @Test
    fun `DELETE message - should fail with 400 for invalid sequence number format`() =
        testApp({ messageWebApi(messageService, chatSseService) }) {
            val user = createTestUser()
            val zone = createTestZone()
            val event = createTestEvent(zoneId = zone.id, organizerId = user.id)
            val token = generateTestToken(user.id)

            val response =
                client.delete("/events/${event.id.value}/chat/abc") {
                    header(HttpHeaders.Authorization, "Bearer $token")
                }
            assertEquals(HttpStatusCode.BadRequest, response.status)
        }

    @Test
    fun `DELETE message - should fail with 400 for negative sequence number`() =
        testApp({ messageWebApi(messageService, chatSseService) }) {
            val user = createTestUser()
            val zone = createTestZone()
            val event = createTestEvent(zoneId = zone.id, organizerId = user.id)
            val token = generateTestToken(user.id)

            val response =
                client.delete("/events/${event.id.value}/chat/-1") {
                    header(HttpHeaders.Authorization, "Bearer $token")
                }
            assertEquals(HttpStatusCode.BadRequest, response.status)
        }
}
