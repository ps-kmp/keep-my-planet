package pt.isel.keepmyplanet.api

import com.auth0.jwt.JWT
import com.auth0.jwt.algorithms.Algorithm
import io.ktor.server.application.install
import io.ktor.server.config.MapApplicationConfig
import io.ktor.server.routing.Routing
import io.ktor.server.routing.routing
import io.ktor.server.sse.SSE
import io.ktor.server.testing.ApplicationTestBuilder
import io.ktor.server.testing.testApplication
import java.util.Date
import kotlin.test.BeforeTest
import kotlinx.datetime.LocalDateTime
import pt.isel.keepmyplanet.domain.common.Description
import pt.isel.keepmyplanet.domain.common.Id
import pt.isel.keepmyplanet.domain.common.Photo
import pt.isel.keepmyplanet.domain.common.Url
import pt.isel.keepmyplanet.domain.event.Event
import pt.isel.keepmyplanet.domain.event.EventStatus
import pt.isel.keepmyplanet.domain.event.Period
import pt.isel.keepmyplanet.domain.event.Title
import pt.isel.keepmyplanet.domain.message.Message
import pt.isel.keepmyplanet.domain.message.MessageContent
import pt.isel.keepmyplanet.domain.user.Email
import pt.isel.keepmyplanet.domain.user.Name
import pt.isel.keepmyplanet.domain.user.Password
import pt.isel.keepmyplanet.domain.user.User
import pt.isel.keepmyplanet.domain.zone.Location
import pt.isel.keepmyplanet.domain.zone.Zone
import pt.isel.keepmyplanet.domain.zone.ZoneSeverity
import pt.isel.keepmyplanet.domain.zone.ZoneStatus
import pt.isel.keepmyplanet.exception.NotFoundException
import pt.isel.keepmyplanet.plugins.configureAuthentication
import pt.isel.keepmyplanet.plugins.configureSerialization
import pt.isel.keepmyplanet.plugins.configureStatusPages
import pt.isel.keepmyplanet.repository.memory.InMemoryEventRepository
import pt.isel.keepmyplanet.repository.memory.InMemoryEventStateChangeRepository
import pt.isel.keepmyplanet.repository.memory.InMemoryMessageRepository
import pt.isel.keepmyplanet.repository.memory.InMemoryPhotoRepository
import pt.isel.keepmyplanet.repository.memory.InMemoryUserDeviceRepository
import pt.isel.keepmyplanet.repository.memory.InMemoryUserRepository
import pt.isel.keepmyplanet.repository.memory.InMemoryZoneRepository
import pt.isel.keepmyplanet.repository.memory.InMemoryZoneStateChangeRepository
import pt.isel.keepmyplanet.security.Pbkdf2PasswordHasher
import pt.isel.keepmyplanet.service.ChatSseService
import pt.isel.keepmyplanet.utils.now

abstract class BaseWebApiTest {
    protected val fakeUserRepository = InMemoryUserRepository()
    protected val fakeZoneRepository = InMemoryZoneRepository()
    protected val fakeEventRepository = InMemoryEventRepository()
    protected val fakeEventStateChangeRepository =
        InMemoryEventStateChangeRepository(fakeUserRepository)
    protected val fakeZoneStateChangeRepository = InMemoryZoneStateChangeRepository()
    protected val fakeMessageRepository = InMemoryMessageRepository()
    protected val fakePhotoRepository = InMemoryPhotoRepository()
    protected val fakeUserDeviceRepository = InMemoryUserDeviceRepository()
    protected val chatSseService = ChatSseService()
    protected val passwordHasher = Pbkdf2PasswordHasher()

    protected val testConfigForNotifications =
        MapApplicationConfig("fcm.projectId" to "test-project-id")

    protected val testJwtSecret = "test-jwt-secret-for-ktor-tests"
    protected val testJwtIssuer = "test-issuer-for-ktor-tests"
    protected val testJwtAudience = "test-audience-for-ktor-tests"
    protected val testJwtRealm = "test-realm-for-ktor-tests"

    @BeforeTest
    fun setup() {
        fakeUserRepository.clear()
        fakeZoneRepository.clear()
        fakeEventRepository.clear()
        fakeMessageRepository.clear()
        fakePhotoRepository.clear()
        fakeUserDeviceRepository.clear()
    }

    protected fun generateTestToken(
        userId: Id,
        expiresInMs: Long = 3_600_000L,
    ): String =
        JWT
            .create()
            .withAudience(testJwtAudience)
            .withIssuer(testJwtIssuer)
            .withClaim("userId", userId.value.toString())
            .withExpiresAt(Date(System.currentTimeMillis() + expiresInMs))
            .sign(Algorithm.HMAC256(testJwtSecret))

    protected suspend fun createTestUser(
        name: Name = Name("user"),
        email: Email = Email("test@example.com"),
        password: Password = Password("Password1!"),
        profilePictureId: Id? = null,
    ): User =
        fakeUserRepository.create(
            User(
                id = Id(0U),
                name = name,
                email = email,
                passwordHash = passwordHasher.hash(password),
                profilePictureId = profilePictureId,
                createdAt = now(),
                updatedAt = now(),
            ),
        )

    protected suspend fun createTestZone(
        reporterId: Id = Id(0U),
        location: Location = Location(0.0, 0.0),
        description: String = "Test Zone",
        status: ZoneStatus = ZoneStatus.REPORTED,
        zoneSeverity: ZoneSeverity = ZoneSeverity.MEDIUM,
        photosIds: Set<Id> = emptySet(),
        eventId: Id? = null,
    ): Zone =
        fakeZoneRepository.create(
            Zone(
                id = Id(0U),
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
        organizerId: Id,
        participantsIds: Set<Id>? = null,
        status: EventStatus = EventStatus.PLANNED,
        title: String = "Test Event Title",
        description: String = "Event for testing",
        period: Period = Period(LocalDateTime(2400, 1, 1, 1, 0), LocalDateTime(2400, 1, 1, 4, 0)),
        maxParticipants: Int? = null,
    ): Event =
        fakeEventRepository.create(
            Event(
                id = Id(0U),
                zoneId = zoneId,
                organizerId = organizerId,
                title = Title(title),
                description = Description(description),
                period = period,
                status = status,
                participantsIds = participantsIds ?: setOf(organizerId),
                maxParticipants = maxParticipants,
                createdAt = now(),
                updatedAt = now(),
            ),
        )

    protected suspend fun createTestPhoto(
        uploaderId: Id,
        url: String = "http://example.com/photo.jpg",
    ): Photo =
        fakePhotoRepository.create(
            Photo(
                id = Id(0U),
                url = Url(url),
                uploaderId = uploaderId,
                uploadedAt = now(),
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
                id = Id(0U),
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
        environment {
            config =
                MapApplicationConfig(
                    "jwt.secret" to testJwtSecret,
                    "jwt.issuer" to testJwtIssuer,
                    "jwt.audience" to testJwtAudience,
                    "jwt.realm" to testJwtRealm,
                )
        }
        application {
            configureAuthentication()
            configureSerialization()
            configureStatusPages()
            install(SSE)
            routing { routingConfig() }
        }
        block()
    }
}
