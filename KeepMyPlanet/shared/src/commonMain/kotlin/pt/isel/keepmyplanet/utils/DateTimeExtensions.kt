package pt.isel.keepmyplanet.utils

import kotlinx.datetime.Clock
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime

val LocalDateTime.Companion.nowUTC: LocalDateTime
    get() = Clock.System.now().toLocalDateTime(TimeZone.UTC)
