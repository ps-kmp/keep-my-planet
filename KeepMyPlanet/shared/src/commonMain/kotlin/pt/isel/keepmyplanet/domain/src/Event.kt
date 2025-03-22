package pt.isel.keepmyplanet.domain.src

data class Event(
    //val id: Long,
    val title: Title,
    val description: Description,
    val start: DateTime,
    val period: Duration,
    //val status: EventStatus,
    //val zone: Zone,
    //val organizer: User,
    //val participants: MutableList<User> = mutableListOf(),
    //val messages: MutableList<Message> = mutableListOf()
)
