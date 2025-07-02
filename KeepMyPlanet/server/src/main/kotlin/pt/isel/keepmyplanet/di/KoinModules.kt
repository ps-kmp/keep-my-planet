package pt.isel.keepmyplanet.di

import app.cash.sqldelight.driver.jdbc.asJdbcDriver
import com.zaxxer.hikari.HikariConfig
import com.zaxxer.hikari.HikariDataSource
import io.ktor.server.application.Application
import io.ktor.server.config.ApplicationConfig
import java.net.URI
import org.koin.dsl.module
import pt.isel.keepmyplanet.db.Database
import pt.isel.keepmyplanet.repository.EventRepository
import pt.isel.keepmyplanet.repository.EventStateChangeRepository
import pt.isel.keepmyplanet.repository.MessageRepository
import pt.isel.keepmyplanet.repository.PhotoRepository
import pt.isel.keepmyplanet.repository.UserDeviceRepository
import pt.isel.keepmyplanet.repository.UserRepository
import pt.isel.keepmyplanet.repository.ZoneRepository
import pt.isel.keepmyplanet.repository.ZoneStateChangeRepository
import pt.isel.keepmyplanet.repository.database.DatabaseEventRepository
import pt.isel.keepmyplanet.repository.database.DatabaseEventStateChangeRepository
import pt.isel.keepmyplanet.repository.database.DatabaseMessageRepository
import pt.isel.keepmyplanet.repository.database.DatabasePhotoRepository
import pt.isel.keepmyplanet.repository.database.DatabaseUserDeviceRepository
import pt.isel.keepmyplanet.repository.database.DatabaseUserRepository
import pt.isel.keepmyplanet.repository.database.DatabaseZoneRepository
import pt.isel.keepmyplanet.repository.database.DatabaseZoneStateChangeRepository
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
import pt.isel.keepmyplanet.service.NotificationService
import pt.isel.keepmyplanet.service.PhotoService
import pt.isel.keepmyplanet.service.UserService
import pt.isel.keepmyplanet.service.ZoneService
import pt.isel.keepmyplanet.service.ZoneStateChangeService
import ptiselkeepmyplanetdb.Event_attendances
import ptiselkeepmyplanetdb.Event_participants
import ptiselkeepmyplanetdb.Event_state_changes
import ptiselkeepmyplanetdb.Events
import ptiselkeepmyplanetdb.Messages
import ptiselkeepmyplanetdb.Photos
import ptiselkeepmyplanetdb.User_devices
import ptiselkeepmyplanetdb.Users
import ptiselkeepmyplanetdb.Zone_photos
import ptiselkeepmyplanetdb.Zone_state_changes
import ptiselkeepmyplanetdb.Zones

fun appModule(application: Application) =
    module {
        single { application.environment.config }

        single<Database> {
            val appConfig = application.environment.config
            val databaseUrl = System.getenv("DATABASE_URL")

            val hikariConfig =
                if (databaseUrl != null) {
                    val dbUri = URI(databaseUrl)
                    val (user, password) = dbUri.userInfo.split(":", limit = 2)
                    val jdbcUrl =
                        "jdbc:postgresql://${dbUri.host}:${dbUri.port}${dbUri.path}?sslmode=require"

                    HikariConfig().apply {
                        this.driverClassName = "org.postgresql.Driver"
                        this.jdbcUrl = jdbcUrl
                        this.username = user
                        this.password = password
                    }
                } else {
                    HikariConfig().apply {
                        this.driverClassName =
                            appConfig.property("storage.driverClassName").getString()
                        this.jdbcUrl = appConfig.property("storage.jdbcURL").getString()
                        this.username = appConfig.property("storage.user").getString()
                        this.password = appConfig.property("storage.password").getString()
                    }
                }

            hikariConfig.apply {
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
                        pending_organizer_idAdapter = IdAdapter,
                        transfer_request_timeAdapter = LocalDateTimeAdapter,
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
                user_devicesAdapter =
                    User_devices.Adapter(
                        idAdapter = IdAdapter,
                        user_idAdapter = IdAdapter,
                        created_atAdapter = LocalDateTimeAdapter,
                    ),
                zone_state_changesAdapter =
                    Zone_state_changes.Adapter(
                        idAdapter = IdAdapter,
                        zone_idAdapter = IdAdapter,
                        new_statusAdapter = ZoneStatusAdapter,
                        changed_byAdapter = IdAdapter,
                        triggered_by_event_idAdapter = IdAdapter,
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
        single<UserDeviceRepository> {
            DatabaseUserDeviceRepository(get<Database>().userDeviceQueries)
        }
        single<ZoneStateChangeRepository> {
            DatabaseZoneStateChangeRepository(get<Database>().zoneStateChangeQueries)
        }

        single { JwtService(get()) }
        single<PasswordHasher> { Pbkdf2PasswordHasher() }
        single { ChatSseService() }
        // single<FileStorageService> { CloudinaryStorageService(get()) }
        single<FileStorageService> {
            val appConfig = get<ApplicationConfig>()
            val baseUrl =
                System.getenv("RENDER_EXTERNAL_URL")
                    ?: appConfig.property("server.baseUrl").getString()
            LocalFileStorageService(baseUrl = baseUrl)
        }

        single { AuthService(get(), get(), get()) }
        single { UserService(get(), get(), get(), get()) }
        single { ZoneService(get(), get(), get(), get(), get()) }
        single { EventService(get(), get(), get(), get(), get(), get(), get()) }
        single { EventStateChangeService(get(), get(), get(), get(), get()) }
        single { ZoneStateChangeService(get(), get()) }
        single { MessageService(get(), get(), get(), get(), get()) }
        single { PhotoService(get(), get(), get()) }
        single { NotificationService(get(), get()) }
    }
