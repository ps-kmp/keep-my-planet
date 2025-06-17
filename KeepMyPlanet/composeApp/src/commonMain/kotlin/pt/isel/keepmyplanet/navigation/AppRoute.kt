package pt.isel.keepmyplanet.navigation

import pt.isel.keepmyplanet.domain.common.Id
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
        val eventId: Id,
    ) : AppRoute()

    data class CreateEvent(
        val zoneId: Id? = null,
    ) : AppRoute()

    data class EditEvent(
        val eventId: Id,
    ) : AppRoute()

    data class ReportZone(
        val latitude: Double,
        val longitude: Double,
    ) : AppRoute()

    data class ZoneDetails(
        val zoneId: Id,
    ) : AppRoute()

    data object Map : AppRoute()

    data object UserProfile : AppRoute()
}
