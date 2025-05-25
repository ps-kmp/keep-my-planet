package pt.isel.keepmyplanet.db

import app.cash.sqldelight.ColumnAdapter
import pt.isel.keepmyplanet.domain.event.EventStatus
import pt.isel.keepmyplanet.domain.zone.ZoneSeverity
import pt.isel.keepmyplanet.domain.zone.ZoneStatus

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
