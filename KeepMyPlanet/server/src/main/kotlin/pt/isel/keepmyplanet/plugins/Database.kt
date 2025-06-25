package pt.isel.keepmyplanet.plugins

import app.cash.sqldelight.db.SqlDriver
import app.cash.sqldelight.driver.jdbc.asJdbcDriver
import com.zaxxer.hikari.HikariConfig
import com.zaxxer.hikari.HikariDataSource
import io.ktor.server.application.Application
import pt.isel.keepmyplanet.db.Database
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
import ptiselkeepmyplanetdb.Event_attendances
import ptiselkeepmyplanetdb.Event_participants
import ptiselkeepmyplanetdb.Event_state_changes
import ptiselkeepmyplanetdb.Events
import ptiselkeepmyplanetdb.Messages
import ptiselkeepmyplanetdb.Photos
import ptiselkeepmyplanetdb.Users
import ptiselkeepmyplanetdb.Zone_photos
import ptiselkeepmyplanetdb.Zones

lateinit var database: Database
    private set

fun Application.configureDatabase() {
    val driverClassName = environment.config.property("storage.driverClassName").getString()
    val jdbcURL = environment.config.property("storage.jdbcURL").getString()
    val user = environment.config.property("storage.user").getString()
    val password = environment.config.property("storage.password").getString()

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
    val driver: SqlDriver = dataSource.asJdbcDriver()

    database =
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
                    sender_nameAdapter = NameAdapter,
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
