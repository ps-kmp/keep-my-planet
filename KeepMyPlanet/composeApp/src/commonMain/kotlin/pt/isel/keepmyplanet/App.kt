package pt.isel.keepmyplanet

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import org.koin.compose.koinInject
import org.koin.core.parameter.parametersOf
import pt.isel.keepmyplanet.navigation.AppRoute
import pt.isel.keepmyplanet.ui.about.AboutScreen
import pt.isel.keepmyplanet.ui.attendance.ManageAttendanceScreen
import pt.isel.keepmyplanet.ui.attendance.MyQrCodeScreen
import pt.isel.keepmyplanet.ui.base.koinViewModel
import pt.isel.keepmyplanet.ui.chat.ChatScreen
import pt.isel.keepmyplanet.ui.components.FullScreenLoading
import pt.isel.keepmyplanet.ui.event.details.EventDetailsScreen
import pt.isel.keepmyplanet.ui.event.forms.CreateEventScreen
import pt.isel.keepmyplanet.ui.event.forms.UpdateEventScreen
import pt.isel.keepmyplanet.ui.event.history.EventStatusHistoryScreen
import pt.isel.keepmyplanet.ui.event.list.EventListScreen
import pt.isel.keepmyplanet.ui.event.participants.ParticipantListScreen
import pt.isel.keepmyplanet.ui.home.HomeScreen
import pt.isel.keepmyplanet.ui.login.LoginScreen
import pt.isel.keepmyplanet.ui.map.MapScreen
import pt.isel.keepmyplanet.ui.profile.UserProfileScreen
import pt.isel.keepmyplanet.ui.register.RegisterScreen
import pt.isel.keepmyplanet.ui.report.ReportZoneScreen
import pt.isel.keepmyplanet.ui.stats.UserStatsScreen
import pt.isel.keepmyplanet.ui.theme.KeepMyPlanetTheme
import pt.isel.keepmyplanet.ui.zone.details.ZoneDetailsScreen
import pt.isel.keepmyplanet.ui.zone.update.UpdateZoneScreen

@Composable
fun App() {
    KeepMyPlanetTheme {
        val appViewModel: AppViewModel = koinInject()
        val uiState by appViewModel.uiState.collectAsState()
        val userSession = uiState.userSession
        val currentRoute = uiState.currentRoute

        if (userSession != null) {
            val currentUserInfo = userSession.userInfo
            when (currentRoute) {
                is AppRoute.Home ->
                    HomeScreen(
                        user = currentUserInfo,
                        onNavigateToEventList = { appViewModel.navigate(AppRoute.EventList) },
                        onNavigateToProfile = { appViewModel.navigate(AppRoute.UserProfile) },
                        onNavigateToMap = { appViewModel.navigate(AppRoute.Map) },
                        onLogout = { appViewModel.logout() },
                        onNavigateToAbout = { appViewModel.navigate(AppRoute.About) },
                    )

                is AppRoute.EventList ->
                    EventListScreen(
                        viewModel = koinViewModel(),
                        onEventSelected = { appViewModel.navigate(AppRoute.EventDetails(it.id)) },
                        onNavigateBack = { appViewModel.navigateBack() },
                        onCreateEventClick = { appViewModel.navigate(AppRoute.CreateEvent()) },
                    )

                is AppRoute.CreateEvent ->
                    CreateEventScreen(
                        viewModel = koinViewModel(),
                        zoneId = currentRoute.zoneId,
                        onEventCreated = {
                            appViewModel.navigateAndReplace(AppRoute.EventDetails(it))
                        },
                        onNavigateBack = { appViewModel.navigateBack() },
                    )

                is AppRoute.EventDetails ->
                    EventDetailsScreen(
                        viewModel = koinViewModel(),
                        eventId = currentRoute.eventId,
                        onNavigateToChat = { appViewModel.navigate(AppRoute.Chat(it)) },
                        onNavigateToEditEvent = { appViewModel.navigate(AppRoute.EditEvent(it)) },
                        onNavigateToUpdateZone = { appViewModel.navigate(AppRoute.UpdateZone(it)) },
                        onNavigateToManageAttendance = {
                            appViewModel.navigate(AppRoute.ManageAttendance(it))
                        },
                        onNavigateToMyQrCode = { userId, organizerName ->
                            appViewModel.navigate(AppRoute.MyQrCode(userId, organizerName))
                        },
                        onNavigateToStatusHistory = {
                            appViewModel.navigate(AppRoute.EventStatusHistory(it))
                        },
                        onNavigateToParticipantList = {
                            appViewModel.navigate(AppRoute.ParticipantList(it))
                        },
                        onNavigateToZoneDetails = {
                            appViewModel.navigate(AppRoute.ZoneDetails(it))
                        },
                        onNavigateBack = { appViewModel.navigateBack() },
                    )

                is AppRoute.ParticipantList ->
                    ParticipantListScreen(
                        viewModel = koinViewModel { parametersOf(currentRoute.eventId) },
                        onNavigateBack = { appViewModel.navigateBack() },
                    )

                is AppRoute.EventStatusHistory ->
                    EventStatusHistoryScreen(
                        viewModel = koinViewModel(),
                        eventId = currentRoute.eventId,
                        onNavigateBack = { appViewModel.navigateBack() },
                    )

                is AppRoute.EditEvent ->
                    UpdateEventScreen(
                        viewModel = koinViewModel(),
                        eventId = currentRoute.eventId,
                        onNavigateBack = { appViewModel.navigateBack() },
                    )

                is AppRoute.Chat ->
                    ChatScreen(
                        viewModel = koinViewModel { parametersOf(currentRoute.info) },
                        onNavigateBack = { appViewModel.navigateBack() },
                    )

                is AppRoute.UserProfile ->
                    UserProfileScreen(
                        viewModel = koinViewModel(),
                        onAccountDeleted = { appViewModel.logout() },
                        onNavigateBack = { appViewModel.navigateBack() },
                        onNavigateToStats = {
                            appViewModel.navigate(AppRoute.UserStats(currentUserInfo.id))
                        },
                        onProfileUpdated = { appViewModel.onProfileUpdated(it) },
                    )

                is AppRoute.ManageAttendance ->
                    ManageAttendanceScreen(
                        viewModel = koinViewModel { parametersOf(currentRoute.eventId) },
                        onNavigateBack = { appViewModel.navigateBack() },
                    )

                is AppRoute.MyQrCode ->
                    MyQrCodeScreen(
                        userId = currentRoute.userId,
                        organizerName = currentRoute.organizerName,
                        onNavigateBack = { appViewModel.navigateBack() },
                    )

                is AppRoute.UserStats ->
                    UserStatsScreen(
                        viewModel = koinViewModel { parametersOf(currentRoute.userId) },
                        userName = currentUserInfo.name.value,
                        onEventSelected = { appViewModel.navigate(AppRoute.EventDetails(it.id)) },
                        onNavigateBack = { appViewModel.navigateBack() },
                    )

                is AppRoute.Map ->
                    MapScreen(
                        viewModel = koinViewModel(),
                        onNavigateToZoneDetails = {
                            appViewModel.navigate(AppRoute.ZoneDetails(it))
                        },
                        onNavigateToReportZone = { lat, lon ->
                            appViewModel.navigate(AppRoute.ReportZone(lat, lon))
                        },
                        onNavigateBack = { appViewModel.navigateBack() },
                    )

                is AppRoute.ZoneDetails ->
                    ZoneDetailsScreen(
                        viewModel = koinViewModel(),
                        zoneId = currentRoute.zoneId,
                        onNavigateToUpdateZone = { appViewModel.navigate(AppRoute.UpdateZone(it)) },
                        onNavigateToCreateEvent = {
                            appViewModel.navigate(AppRoute.CreateEvent(it))
                        },
                        onNavigateToEventDetails = {
                            appViewModel.navigate(AppRoute.EventDetails(it))
                        },
                        onNavigateBack = { appViewModel.navigateBack() },
                    )

                is AppRoute.UpdateZone ->
                    UpdateZoneScreen(
                        viewModel = koinViewModel(),
                        zoneId = currentRoute.zoneId,
                        onNavigateBack = { appViewModel.navigateBack() },
                    )

                is AppRoute.ReportZone ->
                    ReportZoneScreen(
                        viewModel = koinViewModel(),
                        latitude = currentRoute.latitude,
                        longitude = currentRoute.longitude,
                        onNavigateBack = { appViewModel.navigateBack() },
                    )

                is AppRoute.About -> AboutScreen(onNavigateBack = { appViewModel.navigateBack() })
                is AppRoute.Login, is AppRoute.Register -> FullScreenLoading()
                null -> FullScreenLoading()
            }
        } else {
            when (currentRoute) {
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

                else -> FullScreenLoading()
            }
        }
    }
}
