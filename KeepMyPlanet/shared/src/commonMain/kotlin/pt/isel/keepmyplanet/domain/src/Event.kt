package pt.isel.keepmyplanet.domain.src

data class Event(
    val id: Id,
    val title: Title,
    val description: Description,
    val period: Period,
    val status: EventStatus,
    val zone: Zone,
    val organizer: User,
    val participants: List<User>,
    val messages: List<Message>,
)
