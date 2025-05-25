package pt.isel.keepmyplanet.db

import app.cash.sqldelight.ColumnAdapter
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.toKotlinLocalDateTime
import java.time.format.DateTimeFormatterBuilder
import java.time.temporal.ChronoField

object LocalDateTimeAdapter : ColumnAdapter<LocalDateTime, String> {
    private val formatter =
        DateTimeFormatterBuilder()
            .appendPattern("yyyy-MM-dd HH:mm:ss")
            .optionalStart()
            .appendFraction(ChronoField.NANO_OF_SECOND, 0, 9, true)
            .optionalEnd()
            .toFormatter()

    override fun decode(databaseValue: String): LocalDateTime {
        val javaLdt = java.time.LocalDateTime.parse(databaseValue, formatter)
        return javaLdt.toKotlinLocalDateTime()
    }

    override fun encode(value: LocalDateTime) = value.toString()
}
