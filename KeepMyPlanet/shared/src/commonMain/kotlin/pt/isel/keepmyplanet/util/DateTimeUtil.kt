package pt.isel.keepmyplanet.util

import kotlin.time.Duration
import kotlinx.datetime.Clock
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toInstant
import kotlinx.datetime.toLocalDateTime

fun now(): LocalDateTime = Clock.System.now().toLocalDateTime(TimeZone.UTC)

fun LocalDateTime.isAfter(other: LocalDateTime): Boolean = this > other

fun LocalDateTime.isBefore(other: LocalDateTime): Boolean = this < other

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
