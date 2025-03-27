package pt.isel.keepmyplanet.domain.src

data class User(
    val id: Id,
    val name: Name,
    val email: Email, // unique
    val passwordInfo: PasswordInfo,
    val organizedEvents: List<Event>,
    val participatedEvents: List<Event>,
)
