package pt.isel.keepmyplanet.navigation

import pt.isel.keepmyplanet.ui.chat.model.ChatInfo

sealed class AppRoute {
    data object Login : AppRoute()

    data object Register : AppRoute()

    data object Home : AppRoute()

    data object EventList : AppRoute()

    data class Chat(
        val info: ChatInfo,
    ) : AppRoute()

    data class EventDetails(
        val eventId: UInt,
    ) : AppRoute()

    data object CreateEvent : AppRoute()

    data class EditEvent(
        val eventId: UInt,
    ) : AppRoute()

    data object Map : AppRoute()

    data object UserProfile : AppRoute()
}
