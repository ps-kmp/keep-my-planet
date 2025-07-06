package pt.isel.keepmyplanet.repository.database.adapters

import app.cash.sqldelight.ColumnAdapter
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.toJavaLocalDateTime
import kotlinx.datetime.toKotlinLocalDateTime
import pt.isel.keepmyplanet.domain.common.Description
import pt.isel.keepmyplanet.domain.common.Id
import pt.isel.keepmyplanet.domain.event.EventStatus
import pt.isel.keepmyplanet.domain.event.Title
import pt.isel.keepmyplanet.domain.message.MessageContent
import pt.isel.keepmyplanet.domain.user.Email
import pt.isel.keepmyplanet.domain.user.Name
import pt.isel.keepmyplanet.domain.user.PasswordHash
import pt.isel.keepmyplanet.domain.zone.Radius
import pt.isel.keepmyplanet.domain.zone.ZoneSeverity
import pt.isel.keepmyplanet.domain.zone.ZoneStatus

object IdAdapter : ColumnAdapter<Id, Long> {
    override fun decode(databaseValue: Long): Id = Id(databaseValue.toUInt())

    override fun encode(value: Id): Long = value.value.toLong()
}

object RadiusAdapter : ColumnAdapter<Radius, Double> {
    override fun decode(databaseValue: Double): Radius = Radius(databaseValue)

    override fun encode(value: Radius): Double = value.value
}

object EventStatusAdapter : ColumnAdapter<EventStatus, String> {
    override fun decode(databaseValue: String): EventStatus = EventStatus.valueOf(databaseValue)

    override fun encode(value: EventStatus): String = value.name
}

object ZoneStatusAdapter : ColumnAdapter<ZoneStatus, String> {
    override fun decode(databaseValue: String): ZoneStatus = ZoneStatus.valueOf(databaseValue)

    override fun encode(value: ZoneStatus): String = value.name
}

object ZoneSeverityAdapter : ColumnAdapter<ZoneSeverity, String> {
    override fun decode(databaseValue: String): ZoneSeverity = ZoneSeverity.valueOf(databaseValue)

    override fun encode(value: ZoneSeverity): String = value.name
}

object LocalDateTimeAdapter : ColumnAdapter<LocalDateTime, java.time.LocalDateTime> {
    override fun decode(databaseValue: java.time.LocalDateTime): LocalDateTime =
        databaseValue.toKotlinLocalDateTime()

    override fun encode(value: LocalDateTime): java.time.LocalDateTime = value.toJavaLocalDateTime()
}

object TitleAdapter : ColumnAdapter<Title, String> {
    override fun decode(databaseValue: String): Title = Title(databaseValue)

    override fun encode(value: Title): String = value.value
}

object DescriptionAdapter : ColumnAdapter<Description, String> {
    override fun decode(databaseValue: String): Description = Description(databaseValue)

    override fun encode(value: Description): String = value.value
}

object EmailAdapter : ColumnAdapter<Email, String> {
    override fun decode(databaseValue: String): Email = Email(databaseValue)

    override fun encode(value: Email): String = value.value
}

object PasswordHashAdapter : ColumnAdapter<PasswordHash, String> {
    override fun decode(databaseValue: String): PasswordHash = PasswordHash(databaseValue)

    override fun encode(value: PasswordHash): String = value.value
}

object NameAdapter : ColumnAdapter<Name, String> {
    override fun decode(databaseValue: String): Name = Name(databaseValue)

    override fun encode(value: Name): String = value.value
}

object MessageContentAdapter : ColumnAdapter<MessageContent, String> {
    override fun decode(databaseValue: String): MessageContent = MessageContent(databaseValue)

    override fun encode(value: MessageContent): String = value.value
}
