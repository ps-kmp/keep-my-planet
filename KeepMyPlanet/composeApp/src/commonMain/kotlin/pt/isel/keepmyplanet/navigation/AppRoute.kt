package pt.isel.keepmyplanet.navigation

import pt.isel.keepmyplanet.data.model.EventInfo

sealed class AppRoute {
    data object Login : AppRoute()

    data object Register : AppRoute()

    data object Home : AppRoute()

    data object EventList : AppRoute()

    data class Chat(
        val event: EventInfo,
    ) : AppRoute()

    data class EventDetails(
        val eventId: UInt,
    ) : AppRoute()

    data object CreateEvent : AppRoute()

    data object UserProfile : AppRoute()
}
