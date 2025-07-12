package pt.isel.keepmyplanet.navigation

import pt.isel.keepmyplanet.domain.common.Id
import pt.isel.keepmyplanet.domain.message.ChatInfo

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

    data class ParticipantList(
        val eventId: Id,
    ) : AppRoute()

    data class EventStatusHistory(
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
        val radius: Double,
    ) : AppRoute()

    data class ZoneDetails(
        val zoneId: Id,
    ) : AppRoute()

    data class UpdateZone(
        val zoneId: Id,
    ) : AppRoute()

    data object Map : AppRoute()

    data object UserProfile : AppRoute()

    data class ManageAttendance(
        val eventId: Id,
    ) : AppRoute()

    data class MyQrCode(
        val userId: Id,
        val organizerName: String,
    ) : AppRoute()

    data class UserStats(
        val userId: Id,
    ) : AppRoute()

    data class EventStats(
        val eventId: Id,
    ) : AppRoute()

    data object About : AppRoute()

    data object UserManagement : AppRoute()

    data object Downloads : AppRoute()
}

fun isRoutePublic(route: AppRoute): Boolean =
    when (route) {
        is AppRoute.Home,
        is AppRoute.EventList,
        is AppRoute.Map,
        is AppRoute.EventDetails,
        is AppRoute.ZoneDetails,
        is AppRoute.About,
        is AppRoute.Login,
        is AppRoute.Downloads,
        is AppRoute.Register,
        is AppRoute.EventStatusHistory,
        is AppRoute.ParticipantList,
        is AppRoute.EventStats,
        -> true
        else -> false
    }
