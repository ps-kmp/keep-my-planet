package pt.isel.keepmyplanet.api

import io.ktor.client.request.get
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.client.statement.bodyAsText
import io.ktor.http.ContentType
import io.ktor.http.HttpStatusCode
import io.ktor.http.contentType
import io.ktor.serialization.kotlinx.json.json
import io.ktor.server.application.install
import io.ktor.server.plugins.contentnegotiation.ContentNegotiation
import io.ktor.server.routing.routing
import io.ktor.server.testing.ApplicationTestBuilder
import io.ktor.server.testing.testApplication
import kotlinx.serialization.json.Json
import org.junit.jupiter.api.BeforeEach
import pt.isel.keepmyplanet.domain.common.Id
import pt.isel.keepmyplanet.dto.message.AddMessageRequest
import pt.isel.keepmyplanet.dto.message.MessageResponse
import pt.isel.keepmyplanet.plugins.configureStatusPages
import pt.isel.keepmyplanet.repository.mem.InMemoryMessageRepository
import pt.isel.keepmyplanet.repository.mem.InMemoryZoneRepository
import pt.isel.keepmyplanet.services.MessageService
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class MessageWebApiTest {
    private val fakeMessageRepository = InMemoryMessageRepository()
    private val fakeEventRepository = InMemoryZoneRepository()
    private val messageService =
        MessageService(
            fakeMessageRepository,
            fakeEventRepository,
        )
    private val testEventId = Id(1u)
    private val testSenderId = Id(10u)

    private fun testApp(block: suspend ApplicationTestBuilder.() -> Unit) =
        testApplication {
            application {
                install(ContentNegotiation) {
                    json(
                        Json {
                            prettyPrint = true
                            isLenient = true
                            ignoreUnknownKeys = true
                        },
                    )
                }
                configureStatusPages()
                routing {
                    messageWebApi(messageService)
                }
            }
            block()
        }

    @BeforeEach
    fun setup() {
        fakeMessageRepository.clear()
    }

    @Test
    fun `GET messages - should return empty list for event`() =
        testApp {
            val response = client.get("/event/${testEventId.value}/chat")
            assertEquals(HttpStatusCode.OK, response.status)
            assertEquals("[]", response.bodyAsText())
        }

    @Test
    fun `POST add-message - should add a new message successfully`() =
        testApp {
            val requestBody = AddMessageRequest(senderId = testSenderId, content = "Hello World")

            val response =
                client.post("/event/${testEventId.value}/chat/add-message") {
                    contentType(ContentType.Application.Json)
                    setBody(Json.encodeToString(requestBody))
                }

            assertEquals(HttpStatusCode.Created, response.status)
            val responseBody = Json.decodeFromString<MessageResponse>(response.bodyAsText())
            assertEquals("Hello World", responseBody.content)
            assertEquals(testSenderId.value, responseBody.senderId)
        }

    @Test
    fun `GET messages - should return list of messages`() =
        testApp {
            messageService.addMessage(testEventId, testSenderId, "Hi")
            messageService.addMessage(testEventId, testSenderId, "Hello again")

            val response = client.get("/event/${testEventId.value}/chat")
            assertEquals(HttpStatusCode.OK, response.status)
            val responseBody = Json.decodeFromString<List<MessageResponse>>(response.bodyAsText())
            assertEquals(2, responseBody.size)
            assertTrue(responseBody.any { it.content == "Hi" })
            assertTrue(responseBody.any { it.content == "Hello again" })
        }

    @Test
    fun `GET single message - should return specific message by index`() =
        testApp {
            messageService.addMessage(testEventId, testSenderId, "Message 1")
            messageService.addMessage(testEventId, testSenderId, "Message 2")

            val response = client.get("/event/${testEventId.value}/chat/1")
            assertEquals(HttpStatusCode.OK, response.status)
            val responseBody = Json.decodeFromString<MessageResponse>(response.bodyAsText())
            assertEquals("Message 2", responseBody.content)
        }

    @Test
    fun `GET single message - should return 400 for negative index`() =
        testApp {
            val response = client.get("/event/${testEventId.value}/chat/-1")
            assertEquals(HttpStatusCode.BadRequest, response.status)
        }

    @Test
    fun `GET single message - should return 404 for non-existing message`() =
        testApp {
            val response = client.get("/event/${testEventId.value}/chat/99")
            assertEquals(HttpStatusCode.NotFound, response.status)
        }

    @Test
    fun `POST add-message - should return 400 for blank content`() =
        testApp {
            val requestBody = AddMessageRequest(senderId = testSenderId, content = "   ")

            val response =
                client.post("/event/${testEventId.value}/chat/add-message") {
                    contentType(ContentType.Application.Json)
                    setBody(Json.encodeToString(requestBody))
                }

            assertEquals(HttpStatusCode.BadRequest, response.status)
        }

    @Test
    fun `GET messages - should return 400 for invalid event ID`() =
        testApp {
            val response = client.get("/event/abc/chat")
            assertEquals(HttpStatusCode.BadRequest, response.status)
        }
}
