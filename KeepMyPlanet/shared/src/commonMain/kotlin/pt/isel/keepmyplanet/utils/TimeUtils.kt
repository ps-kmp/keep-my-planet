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

fun LocalDateTime.toFormattedString(): String {
    val day = dayOfMonth.toString().padStart(2, '0')
    val month = monthNumber.toString().padStart(2, '0')
    val year = year
    val hour = hour.toString().padStart(2, '0')
    val minute = minute.toString().padStart(2, '0')

    return "$day/$month/$year $hour:$minute"
}

fun formatTimestamp(dateTime: LocalDateTime): String {
    val hour = dateTime.hour.toString().padStart(2, '0')
    val minute = dateTime.minute.toString().padStart(2, '0')
    return "$hour:$minute"
}
