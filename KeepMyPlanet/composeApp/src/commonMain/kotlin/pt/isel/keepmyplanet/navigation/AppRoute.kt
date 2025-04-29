package pt.isel.keepmyplanet.navigation

import pt.isel.keepmyplanet.data.model.EventInfo
import pt.isel.keepmyplanet.data.model.UserInfo

sealed class AppRoute {
    object Login : AppRoute()

    data class Home(
        val user: UserInfo,
    ) : AppRoute()

    data class EventList(
        val user: UserInfo,
    ) : AppRoute()

    data class Chat(
        val user: UserInfo,
        val event: EventInfo,
    ) : AppRoute()
}
