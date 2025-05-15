package pt.isel.keepmyplanet.data.model

data class EventInfo(
    val id: UInt,
    val title: String,
    val description: String,
    val startDate: String,
    val endDate: String,
    val status: String,
)
