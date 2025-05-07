package pt.isel.keepmyplanet.util

import kotlinx.datetime.Clock
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toInstant
import kotlinx.datetime.toLocalDateTime
import kotlin.time.Duration

fun now(): LocalDateTime = Clock.System.now().toLocalDateTime(TimeZone.UTC)

fun LocalDateTime.add(duration: Duration): LocalDateTime {
    val instant = this.toInstant(TimeZone.UTC)
    val newInstant = instant + duration
    return newInstant.toLocalDateTime(TimeZone.UTC)
}

fun LocalDateTime.minus(duration: Duration): LocalDateTime {
    val instant = this.toInstant(TimeZone.UTC)
    val newInstant = instant - duration
    return newInstant.toLocalDateTime(TimeZone.UTC)
}
