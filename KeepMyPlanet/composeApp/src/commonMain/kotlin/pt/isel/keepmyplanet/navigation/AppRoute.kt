package pt.isel.keepmyplanet.navigation

import pt.isel.keepmyplanet.data.model.EventInfo

sealed class AppRoute {
    data object Login : AppRoute()

    data object Home : AppRoute()

    data object EventList : AppRoute()

    data class Chat(
        val event: EventInfo,
    ) : AppRoute()

    data object UserProfile : AppRoute()
}
