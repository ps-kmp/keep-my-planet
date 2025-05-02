package pt.isel.keepmyplanet.api

import io.ktor.client.request.HttpRequestBuilder
import io.ktor.client.request.header
import io.ktor.server.application.install
import io.ktor.server.routing.Routing
import io.ktor.server.routing.routing
import io.ktor.server.sse.SSE
import io.ktor.server.testing.ApplicationTestBuilder
import io.ktor.server.testing.testApplication
import kotlinx.datetime.LocalDateTime
import org.junit.jupiter.api.BeforeEach
import pt.isel.keepmyplanet.domain.common.Description
import pt.isel.keepmyplanet.domain.common.Id
import pt.isel.keepmyplanet.domain.common.Location
import pt.isel.keepmyplanet.domain.event.Event
import pt.isel.keepmyplanet.domain.event.EventStatus
import pt.isel.keepmyplanet.domain.event.Period
import pt.isel.keepmyplanet.domain.event.Title
import pt.isel.keepmyplanet.domain.message.Message
import pt.isel.keepmyplanet.domain.message.MessageContent
import pt.isel.keepmyplanet.domain.user.Email
import pt.isel.keepmyplanet.domain.user.Name
import pt.isel.keepmyplanet.domain.user.PasswordInfo
import pt.isel.keepmyplanet.domain.user.User
import pt.isel.keepmyplanet.domain.zone.Zone
import pt.isel.keepmyplanet.domain.zone.ZoneSeverity
import pt.isel.keepmyplanet.domain.zone.ZoneStatus
import pt.isel.keepmyplanet.errors.NotFoundException
import pt.isel.keepmyplanet.plugins.configureSerialization
import pt.isel.keepmyplanet.plugins.configureStatusPages
import pt.isel.keepmyplanet.repository.mem.InMemoryEventRepository
import pt.isel.keepmyplanet.repository.mem.InMemoryMessageRepository
import pt.isel.keepmyplanet.repository.mem.InMemoryUserRepository
import pt.isel.keepmyplanet.repository.mem.InMemoryZoneRepository
import pt.isel.keepmyplanet.service.ChatSseService
import pt.isel.keepmyplanet.util.now

abstract class BaseWebApiTest {
    protected val fakeUserRepository = InMemoryUserRepository()
    protected val fakeZoneRepository = InMemoryZoneRepository()
    protected val fakeEventRepository = InMemoryEventRepository(fakeZoneRepository)
    protected val fakeMessageRepository = InMemoryMessageRepository()
    protected val chatSseService = ChatSseService()

    @BeforeEach
    fun setup() {
        fakeUserRepository.clear()
        fakeZoneRepository.clear()
        fakeEventRepository.clear()
        fakeMessageRepository.clear()
    }

    protected suspend fun createTestUser(
        name: Name = Name("user"),
        email: Email = Email("test@example.com"),
        passwordInfo: PasswordInfo = PasswordInfo("Password1!"),
        profilePictureId: Id? = null,
    ): User =
        fakeUserRepository.create(
            User(
                id = Id(1U),
                name = name,
                email = email,
                passwordInfo = passwordInfo,
                profilePictureId = profilePictureId,
                createdAt = now(),
                updatedAt = now(),
            ),
        )

    protected suspend fun createTestZone(
        reporterId: Id = Id(1U),
        location: Location = Location(0.0, 0.0),
        description: String = "Test Zone",
        status: ZoneStatus = ZoneStatus.REPORTED,
        zoneSeverity: ZoneSeverity = ZoneSeverity.MEDIUM,
        photosIds: Set<Id> = emptySet(),
        eventId: Id? = null,
    ): Zone =
        fakeZoneRepository.create(
            Zone(
                id = Id(1U),
                location = location,
                description = Description(description),
                reporterId = reporterId,
                status = status,
                zoneSeverity = zoneSeverity,
                photosIds = photosIds,
                eventId = eventId,
                createdAt = now(),
                updatedAt = now(),
            ),
        )

    protected suspend fun createTestEvent(
        zoneId: Id,
        organizerId: Id = Id(1U),
        participantsIds: Set<Id> = setOf(Id(2U)),
        status: EventStatus = EventStatus.PLANNED,
        title: String = "Test Event Title",
        description: String = "Event for testing",
        period: Period = Period(LocalDateTime(2400, 1, 1, 1, 0), LocalDateTime(2400, 1, 1, 4, 0)),
        maxParticipants: Int? = null,
    ): Event =
        fakeEventRepository.create(
            Event(
                id = Id(1U),
                zoneId = zoneId,
                organizerId = organizerId,
                title = Title(title),
                description = Description(description),
                period = period,
                status = status,
                participantsIds = participantsIds,
                maxParticipants = maxParticipants,
                createdAt = now(),
                updatedAt = now(),
            ),
        )

    protected suspend fun linkEventToZone(
        zoneId: Id,
        eventId: Id,
    ) {
        val zone =
            fakeZoneRepository.getById(zoneId)
                ?: throw NotFoundException("Zone '$zoneId' not found")

        val updatedZone =
            zone.copy(eventId = eventId, status = ZoneStatus.CLEANING_SCHEDULED, updatedAt = now())

        fakeZoneRepository.update(updatedZone)
    }

    protected suspend fun createTestMessage(
        eventId: Id,
        senderId: Id,
        senderName: Name,
        content: String = "Test message content",
        timestamp: LocalDateTime = now(),
    ): Message =
        fakeMessageRepository.create(
            Message(
                id = Id(1U),
                eventId = eventId,
                senderId = senderId,
                senderName = senderName,
                content = MessageContent(content),
                timestamp = timestamp,
                chatPosition = -1,
            ),
        )

    protected fun testApp(
        routingConfig: Routing.() -> Unit,
        block: suspend ApplicationTestBuilder.() -> Unit,
    ) = testApplication {
        application {
            configureSerialization()
            configureStatusPages()
            install(SSE)
            routing { routingConfig() }
        }
        block()
    }

    protected fun HttpRequestBuilder.mockUser(userId: Id) {
        header("X-Mock-User-Id", userId.value.toString())
    }
}
