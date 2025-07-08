package pt.isel.keepmyplanet

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import org.koin.compose.koinInject
import pt.isel.keepmyplanet.navigation.AppRoute
import pt.isel.keepmyplanet.ui.about.AboutScreen
import pt.isel.keepmyplanet.ui.admin.UserListScreen
import pt.isel.keepmyplanet.ui.attendance.ManageAttendanceScreen
import pt.isel.keepmyplanet.ui.attendance.MyQrCodeScreen
import pt.isel.keepmyplanet.ui.base.koinViewModel
import pt.isel.keepmyplanet.ui.chat.ChatScreen
import pt.isel.keepmyplanet.ui.chat.ChatViewModel
import pt.isel.keepmyplanet.ui.components.FullScreenLoading
import pt.isel.keepmyplanet.ui.event.details.EventDetailsScreen
import pt.isel.keepmyplanet.ui.event.forms.CreateEventScreen
import pt.isel.keepmyplanet.ui.event.forms.UpdateEventScreen
import pt.isel.keepmyplanet.ui.event.history.EventStatusHistoryScreen
import pt.isel.keepmyplanet.ui.event.list.EventListScreen
import pt.isel.keepmyplanet.ui.event.participants.ParticipantListScreen
import pt.isel.keepmyplanet.ui.event.participants.ParticipantListViewModel
import pt.isel.keepmyplanet.ui.event.stats.EventStatsScreen
import pt.isel.keepmyplanet.ui.event.stats.EventStatsViewModel
import pt.isel.keepmyplanet.ui.home.HomeScreen
import pt.isel.keepmyplanet.ui.login.LoginScreen
import pt.isel.keepmyplanet.ui.map.MapScreen
import pt.isel.keepmyplanet.ui.profile.UserProfileScreen
import pt.isel.keepmyplanet.ui.register.RegisterScreen
import pt.isel.keepmyplanet.ui.report.ReportZoneScreen
import pt.isel.keepmyplanet.ui.stats.UserStatsScreen
import pt.isel.keepmyplanet.ui.stats.UserStatsViewModel
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

        when (currentRoute) {
            is AppRoute.Login -> {
                LoginScreen(
                    viewModel = koinViewModel(),
                    onNavigateToRegister = { appViewModel.navigate(AppRoute.Register) },
                    onContinueAsGuest = { appViewModel.navigateAndReplace(AppRoute.Home) },
                    onLoginSuccess = { appViewModel.updateSession(it) },
                )
            }

            is AppRoute.Register -> {
                RegisterScreen(
                    viewModel = koinViewModel(),
                    onNavigateToLogin = { appViewModel.navigate(AppRoute.Login) },
                )
            }

            is AppRoute.Home ->
                HomeScreen(
                    onNavigateToHome = { appViewModel.navigateToHome() },
                    onNavigateToLogin = { appViewModel.navigate(AppRoute.Login) },
                    onNavigateToRegister = { appViewModel.navigate(AppRoute.Register) },
                    onNavigateToEventList = { appViewModel.navigate(AppRoute.EventList) },
                    onNavigateToProfile = { appViewModel.navigate(AppRoute.UserProfile) },
                    onNavigateToMap = { appViewModel.navigate(AppRoute.Map) },
                    onLogout = { appViewModel.logout() },
                    onNavigateToAbout = { appViewModel.navigate(AppRoute.About) },
                    onNavigateToEventDetails = { eventId ->
                        appViewModel.navigate(AppRoute.EventDetails(eventId))
                    },
                    onNavigateToZoneDetails = { zoneId ->
                        appViewModel.navigate(AppRoute.ZoneDetails(zoneId))
                    },
                    onNavigateToUserManagement = {
                        appViewModel.navigate(AppRoute.UserManagement)
                    },
                )

            is AppRoute.EventList ->
                EventListScreen(
                    viewModel = koinViewModel(),
                    onNavigateToHome = { appViewModel.navigateToHome() },
                    onEventSelected = { appViewModel.navigate(AppRoute.EventDetails(it.id)) },
                    onNavigateBack = { appViewModel.navigateBack() },
                    onCreateEventClick = { appViewModel.navigate(AppRoute.CreateEvent()) },
                )

            is AppRoute.CreateEvent ->
                CreateEventScreen(
                    viewModel = koinViewModel(),
                    onNavigateToHome = { appViewModel.navigateToHome() },
                    zoneId = currentRoute.zoneId,
                    onEventCreated = {
                        appViewModel.navigateAndReplace(AppRoute.EventDetails(it))
                    },
                    onNavigateBack = { appViewModel.navigateBack() },
                )

            is AppRoute.EventDetails ->
                EventDetailsScreen(
                    viewModel = koinViewModel(),
                    onNavigateToHome = { appViewModel.navigateToHome() },
                    onNavigateToLogin = { appViewModel.navigate(AppRoute.Login) },
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
                    onNavigateToEventStats = {
                        appViewModel.navigate(AppRoute.EventStats(it))
                    },
                    onNavigateToZoneDetails = {
                        appViewModel.navigate(AppRoute.ZoneDetails(it))
                    },
                    onNavigateBack = { appViewModel.navigateBack() },
                )

            is AppRoute.ParticipantList -> {
                val viewModel: ParticipantListViewModel = koinViewModel()
                LaunchedEffect(currentRoute.eventId) {
                    viewModel.loadParticipants(currentRoute.eventId)
                }
                ParticipantListScreen(
                    viewModel = viewModel,
                    eventId = currentRoute.eventId,
                    onNavigateToHome = { appViewModel.navigateToHome() },
                    onNavigateBack = { appViewModel.navigateBack() },
                )
            }

            is AppRoute.EventStatusHistory ->
                EventStatusHistoryScreen(
                    viewModel = koinViewModel(),
                    eventId = currentRoute.eventId,
                    onNavigateToHome = { appViewModel.navigateToHome() },
                    onNavigateBack = { appViewModel.navigateBack() },
                )

            is AppRoute.EditEvent ->
                UpdateEventScreen(
                    viewModel = koinViewModel(),
                    eventId = currentRoute.eventId,
                    onNavigateToHome = { appViewModel.navigateToHome() },
                    onNavigateBack = { appViewModel.navigateBack() },
                )

            is AppRoute.Chat -> {
                val viewModel: ChatViewModel = koinViewModel()
                LaunchedEffect(currentRoute.info) {
                    viewModel.load(currentRoute.info)
                }
                ChatScreen(
                    viewModel = viewModel,
                    onNavigateToHome = { appViewModel.navigateToHome() },
                    onNavigateBack = { appViewModel.navigateBack() },
                )
            }

            is AppRoute.UserProfile ->
                UserProfileScreen(
                    viewModel = koinViewModel(),
                    onNavigateToHome = { appViewModel.navigateToHome() },
                    onAccountDeleted = { appViewModel.logout() },
                    onNavigateBack = { appViewModel.navigateBack() },
                    onNavigateToStats = {
                        userSession?.userInfo?.id?.let {
                            appViewModel.navigate(AppRoute.UserStats(it))
                        }
                    },
                    onProfileUpdated = { appViewModel.onProfileUpdated(it) },
                )

            is AppRoute.ManageAttendance -> {
                val viewModel =
                    koinViewModel<pt.isel.keepmyplanet.ui.attendance.ManageAttendanceViewModel>()
                LaunchedEffect(currentRoute.eventId) {
                    viewModel.loadInitialData(currentRoute.eventId)
                }
                ManageAttendanceScreen(
                    viewModel = viewModel,
                    eventId = currentRoute.eventId,
                    onNavigateToHome = { appViewModel.navigateToHome() },
                    onNavigateBack = { appViewModel.navigateBack() },
                )
            }

            is AppRoute.MyQrCode ->
                MyQrCodeScreen(
                    userId = currentRoute.userId,
                    onNavigateToHome = { appViewModel.navigateToHome() },
                    organizerName = currentRoute.organizerName,
                    onNavigateBack = { appViewModel.navigateBack() },
                )

            is AppRoute.UserStats -> {
                val viewModel: UserStatsViewModel = koinViewModel()
                LaunchedEffect(currentRoute.userId) {
                    viewModel.loadInitialData(currentRoute.userId)
                }
                UserStatsScreen(
                    viewModel = viewModel,
                    userId = currentRoute.userId,
                    userName = userSession?.userInfo?.name?.value ?: "User",
                    onNavigateToHome = { appViewModel.navigateToHome() },
                    onEventSelected = { appViewModel.navigate(AppRoute.EventDetails(it.id)) },
                    onNavigateBack = { appViewModel.navigateBack() },
                )
            }

            is AppRoute.EventStats -> {
                val viewModel: EventStatsViewModel = koinViewModel()
                LaunchedEffect(currentRoute.eventId) {
                    viewModel.loadStats(currentRoute.eventId)
                }
                EventStatsScreen(
                    viewModel = viewModel,
                    eventId = currentRoute.eventId,
                    onNavigateToHome = { appViewModel.navigateToHome() },
                    onNavigateBack = { appViewModel.navigateBack() },
                )
            }

            is AppRoute.Map ->
                MapScreen(
                    viewModel = koinViewModel(),
                    onNavigateToHome = { appViewModel.navigateToHome() },
                    onNavigateToZoneDetails = {
                        appViewModel.navigate(AppRoute.ZoneDetails(it))
                    },
                    onNavigateToReportZone = { lat, lon, radius ->
                        appViewModel.navigate(AppRoute.ReportZone(lat, lon, radius))
                    },
                    onNavigateToLogin = { appViewModel.navigate(AppRoute.Login) },
                    onNavigateBack = { appViewModel.navigateBack() },
                )

            is AppRoute.ZoneDetails ->
                ZoneDetailsScreen(
                    viewModel = koinViewModel(),
                    onNavigateToHome = { appViewModel.navigateToHome() },
                    onNavigateToLogin = { appViewModel.navigate(AppRoute.Login) },
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
                    onNavigateToHome = { appViewModel.navigateToHome() },
                    zoneId = currentRoute.zoneId,
                    onNavigateBack = { appViewModel.navigateBack() },
                )

            is AppRoute.ReportZone ->
                ReportZoneScreen(
                    viewModel = koinViewModel(),
                    onNavigateToHome = { appViewModel.navigateToHome() },
                    latitude = currentRoute.latitude,
                    longitude = currentRoute.longitude,
                    radius = currentRoute.radius,
                    onNavigateBack = { appViewModel.navigateBack() },
                )

            is AppRoute.UserManagement ->
                UserListScreen(
                    onNavigateToHome = { appViewModel.navigateToHome() },
                    onNavigateBack = { appViewModel.navigateBack() },
                )

            is AppRoute.About ->
                AboutScreen(
                    onNavigateToHome = { appViewModel.navigateToHome() },
                    onNavigateBack = { appViewModel.navigateBack() },
                )
            null -> FullScreenLoading()
        }
    }
}
