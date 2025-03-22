package pt.isel.keepmyplanet.domain.src

data class User(
    val name: Name,
    val email: Email, //unique
    val password: Password,
    //val reportedZones: MutableList<Zone> = mutableListOf(),
    //val organizedEvents: MutableList<Event> = mutableListOf(),
    //val participatedEvents: MutableList<Event> = mutableListOf(),
    //val sentMessages: MutableList<Message> = mutableListOf()
)
