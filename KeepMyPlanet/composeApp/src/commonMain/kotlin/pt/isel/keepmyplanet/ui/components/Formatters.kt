package pt.isel.keepmyplanet.ui.components

import kotlinx.datetime.LocalDateTime

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
