package pt.isel.keepmyplanet.ui.event.list.components

import kotlinx.datetime.LocalDateTime

fun LocalDateTime.toFormattedString(): String {
    val day = dayOfMonth.toString().padStart(2, '0')
    val month = monthNumber.toString().padStart(2, '0')
    val year = year
    val hour = hour.toString().padStart(2, '0')
    val minute = minute.toString().padStart(2, '0')

    return "$day/$month/$year $hour:$minute"
}

fun String.toFormattedDateTime(): String {
    val dateTime = LocalDateTime.parse(this)
    return dateTime.toFormattedString()
}
