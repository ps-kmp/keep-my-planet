package pt.isel.keepmyplanet

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import org.koin.compose.koinInject
import pt.isel.keepmyplanet.navigation.AppRoute
import pt.isel.keepmyplanet.navigation.NavDirection
import pt.isel.keepmyplanet.ui.about.AboutScreen
import pt.isel.keepmyplanet.ui.admin.UserListScreen
import pt.isel.keepmyplanet.ui.attendance.ManageAttendanceScreen
import pt.isel.keepmyplanet.ui.attendance.ManageAttendanceViewModel
import pt.isel.keepmyplanet.ui.attendance.MyQrCodeScreen
import pt.isel.keepmyplanet.ui.base.koinViewModel
import pt.isel.keepmyplanet.ui.chat.ChatScreen
import pt.isel.keepmyplanet.ui.chat.ChatViewModel
import pt.isel.keepmyplanet.ui.components.FullScreenLoading
import pt.isel.keepmyplanet.ui.downloads.DownloadsScreen
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
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = MaterialTheme.colorScheme.background,
        ) {
            val appViewModel: AppViewModel = koinInject()
            val uiState by appViewModel.uiState.collectAsState()
            val userSession = uiState.userSession
            val currentRoute = uiState.currentRoute

            AnimatedContent(
                targetState = currentRoute,
                transitionSpec = {
                    val forwardTransition =
                        slideInHorizontally { width -> width } + fadeIn() togetherWith
                            slideOutHorizontally { width -> -width } + fadeOut()

                    val backwardTransition =
                        slideInHorizontally { width -> -width } + fadeIn() togetherWith
                            slideOutHorizontally { width -> width } + fadeOut()

                    val replaceTransition =
                        fadeIn(
                            animationSpec = tween(300),
                        ) togetherWith fadeOut(animationSpec = tween(300))

                    when (uiState.navDirection) {
                        NavDirection.FORWARD -> forwardTransition
                        NavDirection.BACKWARD -> backwardTransition
                        NavDirection.REPLACE -> replaceTransition
                    }
                },
                label = "ScreenTransition",
            ) { targetRoute ->
                when (targetRoute) {
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
                            routeKey = targetRoute.toString(),
                            onNavigateToHome = { appViewModel.navigateToHome() },
                            onNavigateToLogin = { appViewModel.navigate(AppRoute.Login) },
                            onNavigateToRegister = { appViewModel.navigate(AppRoute.Register) },
                            onNavigateToEventList = { appViewModel.navigate(AppRoute.EventList) },
                            onNavigateToProfile = { appViewModel.navigate(AppRoute.UserProfile) },
                            onNavigateToMap = { appViewModel.navigate(AppRoute.Map()) },
                            onLogout = { appViewModel.logout() },
                            onNavigateToAbout = { appViewModel.navigate(AppRoute.About) },
                            onNavigateToEventDetails = {
                                appViewModel.navigate(AppRoute.EventDetails(it))
                            },
                            onNavigateToZoneDetails = {
                                appViewModel.navigate(AppRoute.ZoneDetails(it))
                            },
                            onNavigateToUserManagement = {
                                appViewModel.navigate(AppRoute.UserManagement)
                            },
                            onNavigateToDownloads = {
                                appViewModel.navigate(AppRoute.Downloads)
                            },
                        )

                    is AppRoute.EventList ->
                        EventListScreen(
                            routeKey = targetRoute.toString(),
                            viewModel = koinViewModel(),
                            onNavigateToHome = { appViewModel.navigateToHome() },
                            onEventSelected = {
                                appViewModel.navigate(AppRoute.EventDetails(it.id))
                            },
                            onNavigateBack = { appViewModel.navigateBack() },
                        )

                    is AppRoute.CreateEvent ->
                        CreateEventScreen(
                            routeKey = targetRoute.toString(),
                            viewModel = koinViewModel(),
                            onNavigateToHome = { appViewModel.navigateToHome() },
                            zoneId = targetRoute.zoneId,
                            onEventCreated = {
                                appViewModel.navigateAndReplace(AppRoute.EventDetails(it))
                            },
                            onNavigateBack = { appViewModel.navigateBack() },
                        )

                    is AppRoute.EventDetails ->
                        EventDetailsScreen(
                            routeKey = targetRoute.toString(),
                            viewModel = koinViewModel(),
                            onNavigateToHome = { appViewModel.navigateToHome() },
                            onNavigateToLogin = { appViewModel.navigate(AppRoute.Login) },
                            eventId = targetRoute.eventId,
                            onNavigateToChat = { appViewModel.navigate(AppRoute.Chat(it)) },
                            onNavigateToEditEvent = {
                                appViewModel.navigate(AppRoute.EditEvent(it))
                            },
                            onNavigateToUpdateZone = {
                                appViewModel.navigate(AppRoute.UpdateZone(it))
                            },
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
                        LaunchedEffect(targetRoute.eventId) {
                            viewModel.loadParticipants(targetRoute.eventId)
                        }
                        ParticipantListScreen(
                            routeKey = targetRoute.toString(),
                            viewModel = viewModel,
                            eventId = targetRoute.eventId,
                            onNavigateToHome = { appViewModel.navigateToHome() },
                            onNavigateBack = { appViewModel.navigateBack() },
                        )
                    }

                    is AppRoute.EventStatusHistory ->
                        EventStatusHistoryScreen(
                            routeKey = targetRoute.toString(),
                            viewModel = koinViewModel(),
                            eventId = targetRoute.eventId,
                            onNavigateToHome = { appViewModel.navigateToHome() },
                            onNavigateBack = { appViewModel.navigateBack() },
                        )

                    is AppRoute.EditEvent ->
                        UpdateEventScreen(
                            routeKey = targetRoute.toString(),
                            viewModel = koinViewModel(),
                            eventId = targetRoute.eventId,
                            onNavigateToHome = { appViewModel.navigateToHome() },
                            onNavigateBack = { appViewModel.navigateBack() },
                        )

                    is AppRoute.Chat -> {
                        val viewModel: ChatViewModel = koinViewModel()
                        LaunchedEffect(targetRoute.info) {
                            viewModel.load(targetRoute.info)
                        }
                        ChatScreen(
                            viewModel = viewModel,
                            onNavigateToHome = { appViewModel.navigateToHome() },
                            onNavigateBack = { appViewModel.navigateBack() },
                        )
                    }

                    is AppRoute.UserProfile ->
                        UserProfileScreen(
                            routeKey = targetRoute.toString(),
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
                        val viewModel: ManageAttendanceViewModel = koinViewModel()
                        LaunchedEffect(targetRoute.eventId) {
                            viewModel.loadInitialData(targetRoute.eventId)
                        }
                        ManageAttendanceScreen(
                            routeKey = targetRoute.toString(),
                            viewModel = viewModel,
                            eventId = targetRoute.eventId,
                            onNavigateToHome = { appViewModel.navigateToHome() },
                            onNavigateBack = { appViewModel.navigateBack() },
                        )
                    }

                    is AppRoute.MyQrCode ->
                        MyQrCodeScreen(
                            userId = targetRoute.userId,
                            onNavigateToHome = { appViewModel.navigateToHome() },
                            organizerName = targetRoute.organizerName,
                            onNavigateBack = { appViewModel.navigateBack() },
                        )

                    is AppRoute.UserStats -> {
                        val viewModel: UserStatsViewModel = koinViewModel()
                        LaunchedEffect(targetRoute.userId) {
                            viewModel.loadInitialData(targetRoute.userId)
                        }
                        UserStatsScreen(
                            routeKey = targetRoute.toString(),
                            viewModel = viewModel,
                            userId = targetRoute.userId,
                            userName = userSession?.userInfo?.name?.value ?: "User",
                            onNavigateToHome = { appViewModel.navigateToHome() },
                            onEventSelected = {
                                appViewModel.navigate(AppRoute.EventDetails(it.id))
                            },
                            onNavigateBack = { appViewModel.navigateBack() },
                        )
                    }

                    is AppRoute.EventStats -> {
                        val viewModel: EventStatsViewModel = koinViewModel()
                        LaunchedEffect(targetRoute.eventId) {
                            viewModel.loadStats(targetRoute.eventId)
                        }
                        EventStatsScreen(
                            viewModel = viewModel,
                            eventId = targetRoute.eventId,
                            onNavigateToHome = { appViewModel.navigateToHome() },
                            onNavigateBack = { appViewModel.navigateBack() },
                        )
                    }

                    is AppRoute.Map ->
                        MapScreen(
                            routeKey = targetRoute.toString(),
                            viewModel = koinViewModel(),
                            onNavigateToHome = { appViewModel.navigateToHome() },
                            onNavigateToZoneDetails = {
                                appViewModel.navigate(AppRoute.ZoneDetails(it))
                            },
                            onNavigateToReportZone = { lat, lon, radius ->
                                appViewModel.navigate(AppRoute.ReportZone(lat, lon, radius))
                            },
                            initialLatitude = targetRoute.latitude,
                            initialLongitude = targetRoute.longitude,
                            onNavigateToLogin = { appViewModel.navigate(AppRoute.Login) },
                            onNavigateBack = { appViewModel.navigateBack() },
                        )

                    is AppRoute.ZoneDetails ->
                        ZoneDetailsScreen(
                            routeKey = targetRoute.toString(),
                            viewModel = koinViewModel(),
                            onNavigateToHome = { appViewModel.navigateToHome() },
                            onNavigateToLogin = { appViewModel.navigate(AppRoute.Login) },
                            zoneId = targetRoute.zoneId,
                            onNavigateToUpdateZone = {
                                appViewModel.navigate(AppRoute.UpdateZone(it))
                            },
                            onNavigateToCreateEvent = {
                                appViewModel.navigate(AppRoute.CreateEvent(it))
                            },
                            onNavigateToEventDetails = {
                                appViewModel.navigate(AppRoute.EventDetails(it))
                            },
                            onNavigateToMap = { lat, lon ->
                                appViewModel.navigate(AppRoute.Map(lat, lon))
                            },
                            onNavigateBack = { appViewModel.navigateBack() },
                        )

                    is AppRoute.UpdateZone ->
                        UpdateZoneScreen(
                            routeKey = targetRoute.toString(),
                            viewModel = koinViewModel(),
                            onNavigateToHome = { appViewModel.navigateToHome() },
                            zoneId = targetRoute.zoneId,
                            onNavigateBack = { appViewModel.navigateBack() },
                        )

                    is AppRoute.ReportZone ->
                        ReportZoneScreen(
                            routeKey = targetRoute.toString(),
                            viewModel = koinViewModel(),
                            onNavigateToHome = { appViewModel.navigateToHome() },
                            latitude = targetRoute.latitude,
                            longitude = targetRoute.longitude,
                            radius = targetRoute.radius,
                            onNavigateBack = { appViewModel.navigateBack() },
                        )

                    is AppRoute.UserManagement ->
                        UserListScreen(
                            routeKey = targetRoute.toString(),
                            onNavigateToHome = { appViewModel.navigateToHome() },
                            onNavigateBack = { appViewModel.navigateBack() },
                        )

                    is AppRoute.About ->
                        AboutScreen(
                            routeKey = targetRoute.toString(),
                            onNavigateToHome = { appViewModel.navigateToHome() },
                            onNavigateBack = { appViewModel.navigateBack() },
                        )

                    is AppRoute.Downloads ->
                        DownloadsScreen(
                            routeKey = targetRoute.toString(),
                            onNavigateToHome = { appViewModel.navigateToHome() },
                            onNavigateBack = { appViewModel.navigateBack() },
                        )

                    null -> FullScreenLoading()
                }
            }
        }
    }
}
