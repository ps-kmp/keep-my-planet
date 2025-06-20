package pt.isel.keepmyplanet

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.CircularProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import pt.isel.keepmyplanet.di.AppContainer
import pt.isel.keepmyplanet.navigation.AppRoute
import pt.isel.keepmyplanet.ui.chat.ChatScreen
import pt.isel.keepmyplanet.ui.event.attendance.ManageAttendanceScreen
import pt.isel.keepmyplanet.ui.event.attendance.MyQrCodeScreen
import pt.isel.keepmyplanet.ui.event.details.EventDetailsScreen
import pt.isel.keepmyplanet.ui.event.forms.CreateEventScreen
import pt.isel.keepmyplanet.ui.event.forms.UpdateEventScreen
import pt.isel.keepmyplanet.ui.event.list.EventListScreen
import pt.isel.keepmyplanet.ui.home.HomeScreen
import pt.isel.keepmyplanet.ui.login.LoginScreen
import pt.isel.keepmyplanet.ui.map.MapScreen
import pt.isel.keepmyplanet.ui.register.RegisterScreen
import pt.isel.keepmyplanet.ui.user.UserProfileScreen
import pt.isel.keepmyplanet.ui.user.stats.UserStatsScreen
import pt.isel.keepmyplanet.ui.zone.details.ZoneDetailsScreen
import pt.isel.keepmyplanet.ui.zone.report.ReportZoneScreen

@Composable
fun App(container: AppContainer = remember { AppContainer() }) {
    val appViewModel = container.appViewModel
    val currRoute by appViewModel.currentRoute.collectAsState()
    val userSession by appViewModel.userSession.collectAsState()

    if (userSession == null && !(currRoute is AppRoute.Login || currRoute is AppRoute.Register)) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator()
        }
        return
    }

    val currentUserInfo = userSession?.userInfo
    val eventListState = rememberLazyListState()

    when (val route = currRoute) {
        is AppRoute.Login -> {
            val loginViewModel = remember { container.getLoginViewModel() }
            LoginScreen(
                viewModel = loginViewModel,
                onNavigateToRegister = { appViewModel.navigate(AppRoute.Register) },
            )
        }

        is AppRoute.Register -> {
            val registerViewModel = remember { container.getRegisterViewModel() }
            RegisterScreen(
                viewModel = registerViewModel,
                onNavigateToLogin = { appViewModel.navigate(AppRoute.Login) },
            )
        }

        is AppRoute.Home -> {
            currentUserInfo?.let { user ->
                HomeScreen(
                    user = user,
                    onNavigateToEventList = { appViewModel.navigate(AppRoute.EventList) },
                    onNavigateToProfile = { appViewModel.navigate(AppRoute.UserProfile) },
                    onNavigateToMap = { appViewModel.navigate(AppRoute.Map) },
                    onLogout = { container.logout() },
                )
            }
        }

        is AppRoute.EventList -> {
            val eventListViewModel = remember { container.getEventListViewModel() }
            EventListScreen(
                viewModel = eventListViewModel,
                listState = eventListState,
                onEventSelected = { appViewModel.navigate(AppRoute.EventDetails(it.id)) },
                onNavigateBack = { appViewModel.navigateBack() },
                onCreateEventClick = { appViewModel.navigate(AppRoute.CreateEvent()) },
            )
        }

        is AppRoute.CreateEvent -> {
            val eventFormViewModel = remember { container.getEventFormViewModel() }
            CreateEventScreen(
                viewModel = eventFormViewModel,
                zoneId = route.zoneId,
                onEventCreated = { appViewModel.navigate(AppRoute.EventDetails(it)) },
                onNavigateBack = { appViewModel.navigateBack() },
            )
        }

        is AppRoute.EventDetails -> {
            currentUserInfo?.let { user ->
                val eventDetailsViewModel = remember { container.getEventDetailsViewModel(user) }
                EventDetailsScreen(
                    viewModel = eventDetailsViewModel,
                    eventId = route.eventId,
                    onNavigateToChat = { appViewModel.navigate(AppRoute.Chat(it)) },
                    onNavigateToEditEvent = { appViewModel.navigate(AppRoute.EditEvent(it)) },
                    onNavigateToManageAttendance = {
                        appViewModel.navigate(
                            AppRoute.ManageAttendance(it),
                        )
                    },
                    onNavigateBack = { appViewModel.navigateBack() },
                    onNavigateToMyQrCode = { appViewModel.navigate(AppRoute.MyQrCode(user.id)) },
                )
            }
        }

        is AppRoute.EditEvent -> {
            val eventFormViewModel = remember { container.getEventFormViewModel() }
            UpdateEventScreen(
                viewModel = eventFormViewModel,
                eventId = route.eventId,
                onNavigateBack = { appViewModel.navigateBack() },
            )
        }

        is AppRoute.Chat -> {
            currentUserInfo?.let { user ->
                val vm = remember(route.info) { container.getChatViewModel(user, route.info) }
                ChatScreen(
                    viewModel = vm,
                    onNavigateBack = { appViewModel.navigateBack() },
                )
            }
        }

        is AppRoute.UserProfile -> {
            currentUserInfo?.let { user ->
                val viewModel = remember(user.id) { container.getUserProfileViewModel(user) }
                UserProfileScreen(
                    viewModel = viewModel,
                    onAccountDeleted = { container.logout() },
                    onNavigateBack = { appViewModel.navigateBack() },
                )
            }
        }

        is AppRoute.ManageAttendance -> {
            val viewModel = remember { container.getManageAttendanceViewModel(route.eventId) }
            ManageAttendanceScreen(
                viewModel = viewModel,
                onNavigateBack = { appViewModel.navigateBack() }
            )
        }

        is AppRoute.MyQrCode -> {
            MyQrCodeScreen(userId = route.userId, onNavigateBack = { appViewModel.navigateBack() })
        }

        is AppRoute.UserStats -> {
            val viewModel = remember { container.getUserStatsViewModel(route.userId) }
            UserStatsScreen(
                viewModel = viewModel,
                onEventSelected = { appViewModel.navigate(AppRoute.EventDetails(it.id)) },
                onNavigateBack = { appViewModel.navigateBack() }
            )
        }

        is AppRoute.Map -> {
            val mapViewModel = remember { container.getMapViewModel() }
            MapScreen(
                viewModel = mapViewModel,
                onNavigateToZoneDetails = { appViewModel.navigate(AppRoute.ZoneDetails(it)) },
                onNavigateToReportZone = { lat, lon ->
                    appViewModel.navigate(AppRoute.ReportZone(lat, lon))
                },
                onNavigateBack = { appViewModel.navigateBack() },
            )
        }

        is AppRoute.ZoneDetails -> {
            val zoneDetailsViewModel = remember { container.getZoneDetailsViewModel() }
            ZoneDetailsScreen(
                viewModel = zoneDetailsViewModel,
                zoneId = route.zoneId,
                onNavigateToCreateEvent = { appViewModel.navigate(AppRoute.CreateEvent(it)) },
                onNavigateToEventDetails = { appViewModel.navigate(AppRoute.EventDetails(it)) },
                onNavigateBack = { appViewModel.navigateBack() },
            )
        }

        is AppRoute.ReportZone -> {
            val reportZoneViewModel = remember { container.getReportZoneViewModel() }
            ReportZoneScreen(
                viewModel = reportZoneViewModel,
                latitude = route.latitude,
                longitude = route.longitude,
                onNavigateBack = { appViewModel.navigateBack() },
            )
        }
    }
}
