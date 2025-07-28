package pt.isel.keepmyplanet.utils

import kotlin.time.Duration
import kotlinx.datetime.Clock
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toInstant
import kotlinx.datetime.toLocalDateTime

fun now(): LocalDateTime = Clock.System.now().toLocalDateTime(TimeZone.UTC)

fun LocalDateTime.plus(duration: Duration): LocalDateTime {
    val instant = this.toInstant(TimeZone.UTC)
    val newInstant = instant + duration
    return newInstant.toLocalDateTime(TimeZone.UTC)
}

fun LocalDateTime.minus(duration: Duration): LocalDateTime {
    val instant = this.toInstant(TimeZone.UTC)
    val newInstant = instant - duration
    return newInstant.toLocalDateTime(TimeZone.UTC)
}

fun LocalDateTime.toLocalFormattedString(): String {
    val utcInstant = this.toInstant(TimeZone.UTC)
    val localDateTime = utcInstant.toLocalDateTime(TimeZone.currentSystemDefault())

    val day = localDateTime.dayOfMonth.toString().padStart(2, '0')
    val month = localDateTime.monthNumber.toString().padStart(2, '0')
    val year = localDateTime.year
    val hour = localDateTime.hour.toString().padStart(2, '0')
    val minute = localDateTime.minute.toString().padStart(2, '0')

    return "$day/$month/$year $hour:$minute"
}

fun formatTimestamp(dateTime: LocalDateTime): String {
    val hour = dateTime.hour.toString().padStart(2, '0')
    val minute = dateTime.minute.toString().padStart(2, '0')
    return "$hour:$minute"
}
