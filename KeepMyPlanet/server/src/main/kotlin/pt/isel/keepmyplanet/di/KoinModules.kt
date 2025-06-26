package pt.isel.keepmyplanet.di

import app.cash.sqldelight.driver.jdbc.asJdbcDriver
import com.zaxxer.hikari.HikariConfig
import com.zaxxer.hikari.HikariDataSource
import io.ktor.server.application.Application
import org.koin.dsl.module
import pt.isel.keepmyplanet.db.Database
import pt.isel.keepmyplanet.repository.EventRepository
import pt.isel.keepmyplanet.repository.EventStateChangeRepository
import pt.isel.keepmyplanet.repository.MessageRepository
import pt.isel.keepmyplanet.repository.PhotoRepository
import pt.isel.keepmyplanet.repository.UserRepository
import pt.isel.keepmyplanet.repository.ZoneRepository
import pt.isel.keepmyplanet.repository.database.DatabaseEventRepository
import pt.isel.keepmyplanet.repository.database.DatabaseEventStateChangeRepository
import pt.isel.keepmyplanet.repository.database.DatabaseMessageRepository
import pt.isel.keepmyplanet.repository.database.DatabasePhotoRepository
import pt.isel.keepmyplanet.repository.database.DatabaseUserRepository
import pt.isel.keepmyplanet.repository.database.DatabaseZoneRepository
import pt.isel.keepmyplanet.repository.database.adapters.DescriptionAdapter
import pt.isel.keepmyplanet.repository.database.adapters.EmailAdapter
import pt.isel.keepmyplanet.repository.database.adapters.EventStatusAdapter
import pt.isel.keepmyplanet.repository.database.adapters.IdAdapter
import pt.isel.keepmyplanet.repository.database.adapters.LocalDateTimeAdapter
import pt.isel.keepmyplanet.repository.database.adapters.MessageContentAdapter
import pt.isel.keepmyplanet.repository.database.adapters.NameAdapter
import pt.isel.keepmyplanet.repository.database.adapters.PasswordHashAdapter
import pt.isel.keepmyplanet.repository.database.adapters.TitleAdapter
import pt.isel.keepmyplanet.repository.database.adapters.ZoneSeverityAdapter
import pt.isel.keepmyplanet.repository.database.adapters.ZoneStatusAdapter
import pt.isel.keepmyplanet.security.PasswordHasher
import pt.isel.keepmyplanet.security.Pbkdf2PasswordHasher
import pt.isel.keepmyplanet.service.AuthService
import pt.isel.keepmyplanet.service.ChatSseService
import pt.isel.keepmyplanet.service.EventService
import pt.isel.keepmyplanet.service.EventStateChangeService
import pt.isel.keepmyplanet.service.FileStorageService
import pt.isel.keepmyplanet.service.JwtService
import pt.isel.keepmyplanet.service.LocalFileStorageService
import pt.isel.keepmyplanet.service.MessageService
import pt.isel.keepmyplanet.service.PhotoService
import pt.isel.keepmyplanet.service.UserService
import pt.isel.keepmyplanet.service.ZoneService
import ptiselkeepmyplanetdb.Event_attendances
import ptiselkeepmyplanetdb.Event_participants
import ptiselkeepmyplanetdb.Event_state_changes
import ptiselkeepmyplanetdb.Events
import ptiselkeepmyplanetdb.Messages
import ptiselkeepmyplanetdb.Photos
import ptiselkeepmyplanetdb.Users
import ptiselkeepmyplanetdb.Zone_photos
import ptiselkeepmyplanetdb.Zones

fun appModule(application: Application) =
    module {
        single { application.environment.config }

        single<Database> {
            val appConfig = application.environment.config
            val driverClassName = appConfig.property("storage.driverClassName").getString()
            val jdbcURL = appConfig.property("storage.jdbcURL").getString()
            val user = appConfig.property("storage.user").getString()
            val password = appConfig.property("storage.password").getString()

            val hikariConfig =
                HikariConfig().apply {
                    this.driverClassName = driverClassName
                    this.jdbcUrl = jdbcURL
                    this.username = user
                    this.password = password
                    maximumPoolSize = 10
                    isAutoCommit = true
                    transactionIsolation = "TRANSACTION_REPEATABLE_READ"
                    validate()
                }

            val dataSource = HikariDataSource(hikariConfig)
            val driver = dataSource.asJdbcDriver()

            Database(
                driver = driver,
                usersAdapter =
                    Users.Adapter(
                        idAdapter = IdAdapter,
                        nameAdapter = NameAdapter,
                        emailAdapter = EmailAdapter,
                        password_hashAdapter = PasswordHashAdapter,
                        profile_picture_idAdapter = IdAdapter,
                        created_atAdapter = LocalDateTimeAdapter,
                        updated_atAdapter = LocalDateTimeAdapter,
                    ),
                zonesAdapter =
                    Zones.Adapter(
                        idAdapter = IdAdapter,
                        descriptionAdapter = DescriptionAdapter,
                        reporter_idAdapter = IdAdapter,
                        event_idAdapter = IdAdapter,
                        statusAdapter = ZoneStatusAdapter,
                        zone_severityAdapter = ZoneSeverityAdapter,
                        created_atAdapter = LocalDateTimeAdapter,
                        updated_atAdapter = LocalDateTimeAdapter,
                    ),
                eventsAdapter =
                    Events.Adapter(
                        idAdapter = IdAdapter,
                        titleAdapter = TitleAdapter,
                        descriptionAdapter = DescriptionAdapter,
                        start_datetimeAdapter = LocalDateTimeAdapter,
                        end_datetimeAdapter = LocalDateTimeAdapter,
                        zone_idAdapter = IdAdapter,
                        organizer_idAdapter = IdAdapter,
                        statusAdapter = EventStatusAdapter,
                        created_atAdapter = LocalDateTimeAdapter,
                        updated_atAdapter = LocalDateTimeAdapter,
                    ),
                messagesAdapter =
                    Messages.Adapter(
                        idAdapter = IdAdapter,
                        event_idAdapter = IdAdapter,
                        sender_idAdapter = IdAdapter,
                        contentAdapter = MessageContentAdapter,
                        timestampAdapter = LocalDateTimeAdapter,
                    ),
                photosAdapter =
                    Photos.Adapter(
                        idAdapter = IdAdapter,
                        uploader_idAdapter = IdAdapter,
                        uploaded_atAdapter = LocalDateTimeAdapter,
                    ),
                event_participantsAdapter =
                    Event_participants.Adapter(
                        event_idAdapter = IdAdapter,
                        user_idAdapter = IdAdapter,
                    ),
                zone_photosAdapter =
                    Zone_photos.Adapter(
                        zone_idAdapter = IdAdapter,
                        photo_idAdapter = IdAdapter,
                    ),
                event_attendancesAdapter =
                    Event_attendances.Adapter(
                        event_idAdapter = IdAdapter,
                        user_idAdapter = IdAdapter,
                        checked_in_atAdapter = LocalDateTimeAdapter,
                    ),
                event_state_changesAdapter =
                    Event_state_changes.Adapter(
                        idAdapter = IdAdapter,
                        event_idAdapter = IdAdapter,
                        new_statusAdapter = EventStatusAdapter,
                        changed_byAdapter = IdAdapter,
                        change_timeAdapter = LocalDateTimeAdapter,
                    ),
            )
        }

        single<UserRepository> { DatabaseUserRepository(get<Database>().userQueries) }
        single<ZoneRepository> { DatabaseZoneRepository(get<Database>().zoneQueries) }
        single<EventRepository> { DatabaseEventRepository(get<Database>().eventQueries) }
        single<EventStateChangeRepository> {
            DatabaseEventStateChangeRepository(get<Database>().eventStateChangeQueries)
        }
        single<MessageRepository> { DatabaseMessageRepository(get<Database>().messageQueries) }
        single<PhotoRepository> { DatabasePhotoRepository(get<Database>().photoQueries) }

        single { JwtService(get()) }
        single<PasswordHasher> { Pbkdf2PasswordHasher() }
        single { ChatSseService() }
        // single<FileStorageService> { CloudinaryStorageService(get()) }
        single<FileStorageService> {
            val appConfig = get<io.ktor.server.config.ApplicationConfig>()
            val baseUrl = appConfig.property("server.baseUrl").getString()
            LocalFileStorageService(baseUrl = baseUrl)
        }

        single { AuthService(get(), get(), get()) }
        single { UserService(get(), get(), get(), get()) }
        single { ZoneService(get(), get(), get(), get()) }
        single { EventService(get(), get(), get(), get()) }
        single { EventStateChangeService(get(), get(), get()) }
        single { MessageService(get(), get(), get(), get()) }
        single { PhotoService(get(), get(), get()) }
    }
