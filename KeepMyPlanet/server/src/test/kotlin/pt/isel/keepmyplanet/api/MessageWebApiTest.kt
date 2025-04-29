package pt.isel.keepmyplanet.api

import io.ktor.client.request.delete
import io.ktor.client.request.get
import io.ktor.client.request.header
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.client.statement.bodyAsText
import io.ktor.http.ContentType
import io.ktor.http.HttpStatusCode
import io.ktor.http.contentType
import io.ktor.server.application.install
import io.ktor.server.routing.routing
import io.ktor.server.sse.SSE
import io.ktor.server.testing.ApplicationTestBuilder
import io.ktor.server.testing.testApplication
import kotlinx.datetime.LocalDateTime
import kotlinx.serialization.json.Json
import org.junit.jupiter.api.BeforeEach
import pt.isel.keepmyplanet.domain.common.Description
import pt.isel.keepmyplanet.domain.common.Id
import pt.isel.keepmyplanet.domain.event.Event
import pt.isel.keepmyplanet.domain.event.EventStatus
import pt.isel.keepmyplanet.domain.event.Period
import pt.isel.keepmyplanet.domain.event.Title
import pt.isel.keepmyplanet.domain.message.Message
import pt.isel.keepmyplanet.domain.message.MessageContent
import pt.isel.keepmyplanet.domain.user.Name
import pt.isel.keepmyplanet.dto.message.CreateMessageRequest
import pt.isel.keepmyplanet.dto.message.MessageResponse
import pt.isel.keepmyplanet.plugins.configureSerialization
import pt.isel.keepmyplanet.plugins.configureStatusPages
import pt.isel.keepmyplanet.repository.mem.InMemoryEventRepository
import pt.isel.keepmyplanet.repository.mem.InMemoryMessageRepository
import pt.isel.keepmyplanet.repository.mem.InMemoryUserRepository
import pt.isel.keepmyplanet.repository.mem.InMemoryZoneRepository
import pt.isel.keepmyplanet.service.ChatSseService
import pt.isel.keepmyplanet.service.MessageService
import pt.isel.keepmyplanet.util.now
import kotlin.test.Test
import kotlin.test.assertEquals

class MessageWebApiTest {
    private val fakeMessageRepository = InMemoryMessageRepository()
    private val fakeZoneRepository = InMemoryZoneRepository()
    private val fakeEventRepository = InMemoryEventRepository(fakeZoneRepository)
    private val fakeUserRepository = InMemoryUserRepository()
    private val chatSseService = ChatSseService()
    private val messageService =
        MessageService(fakeMessageRepository, fakeEventRepository, fakeUserRepository, chatSseService)
    private val testOrganizerId = Id(2u)
    private val testParticipantId = Id(3u)
    private val testParticipantName = Name("user")
    private val nonParticipantId = Id(4u)

    private suspend fun createTestEvent(
        organizer: Id = testOrganizerId,
        participants: Set<Id> = setOf(testParticipantId),
        status: EventStatus = EventStatus.PLANNED,
        zoneId: Id = Id(1u),
        title: String = "Test Event Title",
        description: String = "Event for testing messages",
        maxParticipants: Int? = null,
    ): Event {
        val time = now()
        val period = Period(LocalDateTime(2400, 1, 1, 1, 0), LocalDateTime(2400, 1, 1, 4, 0))
        return fakeEventRepository.create(
            Event(
                id = Id(999u),
                zoneId = zoneId,
                organizerId = organizer,
                title = Title(title),
                description = Description(description),
                period = period,
                status = status,
                participantsIds = participants,
                maxParticipants = maxParticipants,
                createdAt = time,
                updatedAt = time,
            ),
        )
    }

    private suspend fun createTestMessage(
        eventId: Id,
        senderId: Id,
        senderName: Name,
        content: String = "Test message content",
    ): Message =
        fakeMessageRepository.create(
            Message(
                id = Id(999u),
                eventId = eventId,
                senderId = senderId,
                senderName = senderName,
                content = MessageContent(content),
                timestamp = now(),
                chatPosition = -1,
            ),
        )

    @BeforeEach
    fun setup() {
        fakeMessageRepository.clear()
        fakeEventRepository.clear()
    }

    private fun testApp(block: suspend ApplicationTestBuilder.() -> Unit) =
        testApplication {
            application {
                configureSerialization()
                configureStatusPages()
                install(SSE)
                routing { messageWebApi(messageService, chatSseService) }
            }
            block()
        }

    /*
    @Test
    fun `POST message - should create message successfully as organizer`() =
        testApp {
            val event = createTestEvent(organizer = testOrganizerId)
            val requestBody = AddMessageRequest(content = "Hello from organizer!")

            val response =
                client.post("/event/${event.id.value}/chat") {
                    header("X-Test-User-Id", testOrganizerId.value.toString())
                    contentType(ContentType.Application.Json)
                    setBody(Json.encodeToString(requestBody))
                }

            assertEquals(HttpStatusCode.Created, response.status)
            val responseBody = Json.decodeFromString<MessageResponse>(response.bodyAsText())
            assertEquals(event.id.value, responseBody.eventId)
            assertEquals(testOrganizerId.value, responseBody.senderId)
            assertEquals(requestBody.content, responseBody.content)
            assertEquals(0, responseBody.chatPosition) // First message
            assertNotNull(fakeMessageRepository.getById(Id(responseBody.id)))
        }




    @Test
    fun `POST message - should create message successfully as participant`() =
        testApp {
            val event = createTestEvent(participants = setOf(testParticipantId))
            val requestBody = AddMessageRequest(content = "Hi from participant!")

            val response =
                client.post("/event/${event.id.value}/chat") {
                    header("X-Test-User-Id", testParticipantId.value.toString())
                    contentType(ContentType.Application.Json)
                    setBody(Json.encodeToString(requestBody))
                }

            assertEquals(HttpStatusCode.Created, response.status)
            val responseBody = Json.decodeFromString<MessageResponse>(response.bodyAsText())
            assertEquals(testParticipantId.value, responseBody.senderId)
            assertEquals(0, responseBody.chatPosition)
        }



    @Test
    fun `POST message - should assign incremental sequence numbers (chatPosition)`() =
        testApp {
            val event = createTestEvent()
            val requestBody1 = AddMessageRequest(content = "First")
            val requestBody2 = AddMessageRequest(content = "Second")

            client
                .post("/event/${event.id.value}/chat") {
                    header("X-Test-User-Id", testOrganizerId.value.toString())
                    contentType(ContentType.Application.Json)
                    setBody(Json.encodeToString(requestBody1))
                }.apply { assertEquals(HttpStatusCode.Created, status) }

            val response2 =
                client.post("/event/${event.id.value}/chat") {
                    header("X-Test-User-Id", testParticipantId.value.toString())
                    contentType(ContentType.Application.Json)
                    setBody(Json.encodeToString(requestBody2))
                }

            assertEquals(HttpStatusCode.Created, response2.status)
            val responseBody2 = Json.decodeFromString<MessageResponse>(response2.bodyAsText())
            assertEquals(1, responseBody2.chatPosition)

            val messages = fakeMessageRepository.findByEventId(event.id)
            assertEquals(2, messages.size)
            assertEquals(0, messages[0].chatPosition)
            assertEquals(1, messages[1].chatPosition)
        }
     */

    @Test
    fun `POST message - should fail with 404 if event not found`() =
        testApp {
            val nonExistentEventId = 999u
            val requestBody = CreateMessageRequest(content = "Doesn't matter")

            val response =
                client.post("/event/$nonExistentEventId/chat") {
                    header("X-Test-User-Id", testOrganizerId.value.toString())
                    contentType(ContentType.Application.Json)
                    setBody(Json.encodeToString(requestBody))
                }

            assertEquals(HttpStatusCode.NotFound, response.status)
        }

    /*
    @Test
    fun `POST message - should fail with 400 if sender is not organizer or participant`() =
        testApp {
            val event = createTestEvent()
            val requestBody = AddMessageRequest(content = "Intruder message")

            val response =
                client.post("/event/${event.id.value}/chat") {
                    header(
                        "X-Test-User-Id",
                        nonParticipantId.value.toString(),
                    ) // Use non-participant ID
                    contentType(ContentType.Application.Json)
                    setBody(Json.encodeToString(requestBody))
                }

            assertEquals(
                HttpStatusCode.BadRequest,
                response.status,
            ) // Service throws IllegalArgumentException
        }
     */

    @Test
    fun `POST message - should fail with 409 if event is COMPLETED`() =
        testApp {
            val event = createTestEvent(status = EventStatus.COMPLETED)
            val requestBody = CreateMessageRequest(content = "Too late")

            val response =
                client.post("/event/${event.id.value}/chat") {
                    header("X-Test-User-Id", testOrganizerId.value.toString())
                    contentType(ContentType.Application.Json)
                    setBody(Json.encodeToString(requestBody))
                }

            assertEquals(HttpStatusCode.Conflict, response.status)
        }

    @Test
    fun `POST message - should fail with 409 if event is CANCELLED`() =
        testApp {
            val event = createTestEvent(status = EventStatus.CANCELLED)
            val requestBody = CreateMessageRequest(content = "Event cancelled")

            val response =
                client.post("/event/${event.id.value}/chat") {
                    header("X-Test-User-Id", testOrganizerId.value.toString())
                    contentType(ContentType.Application.Json)
                    setBody(Json.encodeToString(requestBody))
                }

            assertEquals(HttpStatusCode.Conflict, response.status)
        }

    @Test
    fun `POST message - should fail with 400 for invalid event ID format`() =
        testApp {
            val requestBody = CreateMessageRequest(content = "Valid content")
            val response =
                client.post("/event/abc/chat") {
                    header("X-Test-User-Id", testOrganizerId.value.toString())
                    contentType(ContentType.Application.Json)
                    setBody(Json.encodeToString(requestBody))
                }
            assertEquals(HttpStatusCode.BadRequest, response.status)
        }

    /*
    @Test
    fun `POST message - should fail with 400 for blank content`() =
        testApp {
            val event = createTestEvent()
            val requestBody = AddMessageRequest(content = "  ")

            val response =
                client.post("/event/${event.id.value}/chat") {
                    header("X-Test-User-Id", testOrganizerId.value.toString())
                    contentType(ContentType.Application.Json)
                    setBody(Json.encodeToString(requestBody))
                }
            assertEquals(HttpStatusCode.BadRequest, response.status)
        }
     */

    @Test
    fun `GET messages - should return empty list for event`() =
        testApp {
            val event = createTestEvent()
            val response = client.get("/event/${event.id.value}/chat")
            assertEquals(HttpStatusCode.OK, response.status)
            assertEquals("[]", response.bodyAsText())
        }

    @Test
    fun `GET messages - should return list of messages sorted by sequence number`() =
        testApp {
            val event = createTestEvent()
            val msg1 = createTestMessage(event.id, testOrganizerId, testParticipantName, "First message")
            val msg2 = createTestMessage(event.id, testParticipantId, testParticipantName, "Second message")
            val msg3 = createTestMessage(event.id, testOrganizerId, testParticipantName, "Third message")

            val response = client.get("/event/${event.id.value}/chat")

            assertEquals(HttpStatusCode.OK, response.status)
            val responseList = Json.decodeFromString<List<MessageResponse>>(response.bodyAsText())

            assertEquals(3, responseList.size)
            assertEquals(msg1.id.value, responseList[0].id)
            assertEquals(0, responseList[0].chatPosition)
            assertEquals(msg2.id.value, responseList[1].id)
            assertEquals(1, responseList[1].chatPosition)
            assertEquals(msg3.id.value, responseList[2].id)
            assertEquals(2, responseList[2].chatPosition)
        }

    @Test
    fun `GET messages - should fail with 404 if event not found`() =
        testApp {
            val nonExistentEventId = 999u
            val response = client.get("/event/$nonExistentEventId/chat")
            assertEquals(HttpStatusCode.NotFound, response.status)
        }

    @Test
    fun `GET messages - should return 400 for invalid event ID`() =
        testApp {
            val response = client.get("/event/abc/chat")
            assertEquals(HttpStatusCode.BadRequest, response.status)
        }

    @Test
    fun `GET single message - should fail with 404 if event not found`() =
        testApp {
            val nonExistentEventId = 999u
            val response = client.get("/event/$nonExistentEventId/chat/0")
            assertEquals(HttpStatusCode.NotFound, response.status)
        }

    @Test
    fun `GET single message - should fail with 404 if sequence number not found`() =
        testApp {
            val event = createTestEvent()
            createTestMessage(event.id, testOrganizerId, testParticipantName) // Creates message with seq 0

            val response =
                client.get("/event/${event.id.value}/chat/5") // Sequence 5 does not exist
            assertEquals(HttpStatusCode.NotFound, response.status)
        }

    @Test
    fun `GET single message - should fail with 400 for invalid event ID format`() =
        testApp {
            val response = client.get("/event/bad-id/chat/0")
            assertEquals(HttpStatusCode.BadRequest, response.status)
        }

    @Test
    fun `GET single message - should fail with 400 for invalid sequence number format`() =
        testApp {
            val event = createTestEvent()
            val response = client.get("/event/${event.id.value}/chat/abc")
            assertEquals(HttpStatusCode.BadRequest, response.status)
        }

    @Test
    fun `GET single message - should fail with 400 for negative sequence number`() =
        testApp {
            val event = createTestEvent()
            val response = client.get("/event/${event.id.value}/chat/-1")
            assertEquals(HttpStatusCode.BadRequest, response.status)
        }

    /*
    @Test
    fun `DELETE message - should succeed when sender deletes own message`() =
        testApp {
            val event = createTestEvent(participants = setOf(testParticipantId))
            val msg = createTestMessage(event.id, testParticipantId)

            val response =
                client.delete("/event/${event.id.value}/chat/${msg.chatPosition}") {
                    header("X-Test-User-Id", testParticipantId.value.toString())
                }

            assertEquals(HttpStatusCode.NoContent, response.status)
            assertNull(
                fakeMessageRepository.findByEventIdAndSequenceNum(event.id, msg.chatPosition),
            )
        }



    @Test
    fun `DELETE message - should succeed when organizer deletes any message`() =
        testApp {
            val event =
                createTestEvent(
                    organizer = testOrganizerId,
                    participants = setOf(testParticipantId),
                )
            val msg = createTestMessage(event.id, testParticipantId)

            val response =
                client.delete("/event/${event.id.value}/chat/${msg.chatPosition}") {
                    header("X-Test-User-Id", testOrganizerId.value.toString())
                }

            assertEquals(HttpStatusCode.NoContent, response.status)
            assertNull(
                fakeMessageRepository.findByEventIdAndSequenceNum(
                    event.id,
                    msg.chatPosition,
                ),
            )
        }




    @Test
    fun `DELETE message - should fail with 403 when non-sender, non-organizer tries to delete`() =
        testApp {
            val event =
                createTestEvent(
                    organizer = testOrganizerId,
                    participants = setOf(testParticipantId),
                )
            val msg = createTestMessage(event.id, testParticipantId)

            val response =
                client.delete("/event/${event.id.value}/chat/${msg.chatPosition}") {
                    header("X-Test-User-Id", nonParticipantId.value.toString())
                }

            assertEquals(HttpStatusCode.Forbidden, response.status)
            assertNotNull(
                fakeMessageRepository.findByEventIdAndSequenceNum(event.id, msg.chatPosition),
            )
        }
     */

    @Test
    fun `DELETE message - should fail with 404 if event not found`() =
        testApp {
            val nonExistentEventId = 999u
            val response =
                client.delete("/event/$nonExistentEventId/chat/0") {
                    header("X-Test-User-Id", testOrganizerId.value.toString())
                }
            assertEquals(HttpStatusCode.NotFound, response.status)
        }

    @Test
    fun `DELETE message - should fail with 404 if sequence number not found`() =
        testApp {
            val event = createTestEvent()
            createTestMessage(event.id, testOrganizerId, testParticipantName) // Creates message with seq 0

            val response =
                client.delete("/event/${event.id.value}/chat/5") {
                    // Sequence 5 does not exist
                    header("X-Test-User-Id", testOrganizerId.value.toString())
                }
            assertEquals(HttpStatusCode.NotFound, response.status)
        }

    @Test
    fun `DELETE message - should fail with 400 for invalid event ID format`() =
        testApp {
            val response =
                client.delete("/event/bad-id/chat/0") {
                    header("X-Test-User-Id", testOrganizerId.value.toString())
                }
            assertEquals(HttpStatusCode.BadRequest, response.status)
        }

    @Test
    fun `DELETE message - should fail with 400 for invalid sequence number format`() =
        testApp {
            val event = createTestEvent()
            val response =
                client.delete("/event/${event.id.value}/chat/abc") {
                    header("X-Test-User-Id", testOrganizerId.value.toString())
                }
            assertEquals(HttpStatusCode.BadRequest, response.status)
        }

    @Test
    fun `DELETE message - should fail with 400 for negative sequence number`() =
        testApp {
            val event = createTestEvent()
            val response =
                client.delete("/event/${event.id.value}/chat/-1") {
                    header("X-Test-User-Id", testOrganizerId.value.toString())
                }
            assertEquals(HttpStatusCode.BadRequest, response.status)
        }
}
