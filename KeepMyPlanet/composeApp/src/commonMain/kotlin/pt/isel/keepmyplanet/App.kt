package pt.isel.keepmyplanet

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.CircularProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import org.koin.compose.koinInject
import org.koin.core.parameter.parametersOf
import pt.isel.keepmyplanet.navigation.AppRoute
import pt.isel.keepmyplanet.ui.about.AboutScreen
import pt.isel.keepmyplanet.ui.attendance.ManageAttendanceScreen
import pt.isel.keepmyplanet.ui.attendance.MyQrCodeScreen
import pt.isel.keepmyplanet.ui.base.koinViewModel
import pt.isel.keepmyplanet.ui.chat.ChatScreen
import pt.isel.keepmyplanet.ui.event.details.EventDetailsScreen
import pt.isel.keepmyplanet.ui.event.forms.CreateEventScreen
import pt.isel.keepmyplanet.ui.event.forms.UpdateEventScreen
import pt.isel.keepmyplanet.ui.event.history.EventStatusHistoryScreen
import pt.isel.keepmyplanet.ui.event.list.EventListScreen
import pt.isel.keepmyplanet.ui.home.HomeScreen
import pt.isel.keepmyplanet.ui.login.LoginScreen
import pt.isel.keepmyplanet.ui.map.MapScreen
import pt.isel.keepmyplanet.ui.profile.UserProfileScreen
import pt.isel.keepmyplanet.ui.register.RegisterScreen
import pt.isel.keepmyplanet.ui.report.ReportZoneScreen
import pt.isel.keepmyplanet.ui.stats.UserStatsScreen
import pt.isel.keepmyplanet.ui.zone.details.ZoneDetailsScreen
import pt.isel.keepmyplanet.ui.zone.update.UpdateZoneScreen

@Composable
fun App() {
    val appViewModel: AppViewModel = koinInject()
    val currRoute by appViewModel.currentRoute.collectAsState()
    val userSession by appViewModel.userSession.collectAsState()

    if (userSession == null && !(currRoute is AppRoute.Login || currRoute is AppRoute.Register)) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator()
        }
        return
    }

    val currentUserInfo = userSession?.userInfo
    when (val route = currRoute) {
        is AppRoute.Login -> {
            LoginScreen(
                viewModel = koinViewModel(),
                onNavigateToRegister = { appViewModel.navigate(AppRoute.Register) },
                onLoginSuccess = { appViewModel.updateSession(it) },
            )
        }

        is AppRoute.Register -> {
            RegisterScreen(
                viewModel = koinViewModel(),
                onNavigateToLogin = { appViewModel.navigate(AppRoute.Login) },
            )
        }

        is AppRoute.Home -> {
            currentUserInfo?.let {
                HomeScreen(
                    user = it,
                    onNavigateToEventList = { appViewModel.navigate(AppRoute.EventList) },
                    onNavigateToProfile = { appViewModel.navigate(AppRoute.UserProfile) },
                    onNavigateToMap = { appViewModel.navigate(AppRoute.Map) },
                    onLogout = { appViewModel.logout() },
                    onNavigateToAbout = { appViewModel.navigate(AppRoute.About) },
                )
            }
        }

        is AppRoute.EventList -> {
            EventListScreen(
                viewModel = koinViewModel(),
                onEventSelected = { appViewModel.navigate(AppRoute.EventDetails(it.id)) },
                onNavigateBack = { appViewModel.navigateBack() },
                onCreateEventClick = { appViewModel.navigate(AppRoute.CreateEvent()) },
            )
        }

        is AppRoute.CreateEvent -> {
            CreateEventScreen(
                viewModel = koinViewModel(),
                zoneId = route.zoneId,
                onEventCreated = { appViewModel.navigateAndReplace(AppRoute.EventDetails(it)) },
                onNavigateBack = { appViewModel.navigateBack() },
            )
        }

        is AppRoute.EventDetails -> {
            currentUserInfo?.let { user ->
                EventDetailsScreen(
                    viewModel = koinViewModel(),
                    eventId = route.eventId,
                    onNavigateToChat = { appViewModel.navigate(AppRoute.Chat(it)) },
                    onNavigateToEditEvent = { appViewModel.navigate(AppRoute.EditEvent(it)) },
                    onNavigateToManageAttendance = {
                        appViewModel.navigate(AppRoute.ManageAttendance(it))
                    },
                    onNavigateToMyQrCode = { userId, organizerName ->
                        appViewModel.navigate(AppRoute.MyQrCode(userId, organizerName))
                    },
                    onNavigateToStatusHistory = {
                        appViewModel.navigate(AppRoute.EventStatusHistory(it))
                    },
                    onNavigateBack = { appViewModel.navigateBack() },
                )
            }
        }

        is AppRoute.EventStatusHistory -> {
            EventStatusHistoryScreen(
                viewModel = koinViewModel(),
                eventId = route.eventId,
                onNavigateBack = { appViewModel.navigateBack() },
            )
        }

        is AppRoute.EditEvent -> {
            UpdateEventScreen(
                viewModel = koinViewModel(),
                eventId = route.eventId,
                onNavigateBack = { appViewModel.navigateBack() },
            )
        }

        is AppRoute.Chat -> {
            currentUserInfo?.let { user ->
                ChatScreen(
                    viewModel = koinViewModel { parametersOf(route.info) },
                    onNavigateBack = { appViewModel.navigateBack() },
                )
            }
        }

        is AppRoute.UserProfile -> {
            currentUserInfo?.let { user ->
                UserProfileScreen(
                    viewModel = koinViewModel(),
                    onAccountDeleted = { appViewModel.logout() },
                    onNavigateBack = { appViewModel.navigateBack() },
                    onNavigateToStats = { appViewModel.navigate(AppRoute.UserStats(user.id)) },
                    onProfileUpdated = { appViewModel.onProfileUpdated(it) },
                )
            }
        }

        is AppRoute.ManageAttendance -> {
            ManageAttendanceScreen(
                viewModel = koinViewModel { parametersOf(route.eventId) },
                onNavigateBack = { appViewModel.navigateBack() },
            )
        }

        is AppRoute.MyQrCode -> {
            MyQrCodeScreen(
                userId = route.userId,
                organizerName = route.organizerName,
                onNavigateBack = { appViewModel.navigateBack() },
            )
        }

        is AppRoute.UserStats -> {
            UserStatsScreen(
                viewModel = koinViewModel { parametersOf(route.userId) },
                userName = userSession?.userInfo?.name?.value ?: "User",
                onEventSelected = { appViewModel.navigate(AppRoute.EventDetails(it.id)) },
                onNavigateBack = { appViewModel.navigateBack() },
            )
        }

        is AppRoute.Map -> {
            MapScreen(
                viewModel = koinViewModel(),
                onNavigateToZoneDetails = { appViewModel.navigate(AppRoute.ZoneDetails(it)) },
                onNavigateToReportZone = { lat, lon ->
                    appViewModel.navigate(AppRoute.ReportZone(lat, lon))
                },
                onNavigateBack = { appViewModel.navigateBack() },
            )
        }

        is AppRoute.ZoneDetails -> {
            ZoneDetailsScreen(
                viewModel = koinViewModel(),
                zoneId = route.zoneId,
                onNavigateToCreateEvent = { appViewModel.navigate(AppRoute.CreateEvent(it)) },
                onNavigateToEventDetails = { appViewModel.navigate(AppRoute.EventDetails(it)) },
                onNavigateBack = { appViewModel.navigateBack() },
                onNavigateToUpdateZone = { zoneId ->
                    appViewModel.navigate(AppRoute.UpdateZone(zoneId))
                },
            )
        }

        is AppRoute.UpdateZone -> {
            UpdateZoneScreen(
                viewModel = koinViewModel(),
                zoneId = route.zoneId,
                onNavigateBack = { appViewModel.navigateBack() },
            )
        }

        is AppRoute.ReportZone -> {
            ReportZoneScreen(
                viewModel = koinViewModel(),
                latitude = route.latitude,
                longitude = route.longitude,
                onNavigateBack = { appViewModel.navigateBack() },
            )
        }

        is AppRoute.About -> {
            AboutScreen(
                onNavigateBack = { appViewModel.navigateBack() },
            )
        }
    }
}
