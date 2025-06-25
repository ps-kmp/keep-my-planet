package pt.isel.keepmyplanet

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.CircularProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import pt.isel.keepmyplanet.di.AppContainer
import pt.isel.keepmyplanet.navigation.AppRoute
import pt.isel.keepmyplanet.ui.attendance.ManageAttendanceScreen
import pt.isel.keepmyplanet.ui.attendance.MyQrCodeScreen
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
import pt.isel.keepmyplanet.ui.zone.ZoneDetailsScreen

@Composable
fun App(container: AppContainer = remember { AppContainer() }) {
    val appViewModel = container.appViewModel
    val currRoute by appViewModel.currentRoute.collectAsState()
    val userSession by appViewModel.userSession.collectAsState()

    DisposableEffect(appViewModel) {
        onDispose {
            appViewModel.onCleared()
        }
    }

    if (userSession == null && !(currRoute is AppRoute.Login || currRoute is AppRoute.Register)) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator()
        }
        return
    }

    val currentUserInfo = userSession?.userInfo
    when (val route = currRoute) {
        is AppRoute.Login -> {
            val viewModel = remember { container.getLoginViewModel() }
            DisposableEffect(viewModel) { onDispose { viewModel.onCleared() } }
            LoginScreen(
                viewModel = viewModel,
                onNavigateToRegister = { appViewModel.navigate(AppRoute.Register) },
                onLoginSuccess = { container.updateSession(it) },
            )
        }

        is AppRoute.Register -> {
            val viewModel = remember { container.getRegisterViewModel() }
            DisposableEffect(viewModel) { onDispose { viewModel.onCleared() } }
            RegisterScreen(
                viewModel = viewModel,
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
                    onLogout = { container.logout() },
                )
            }
        }

        is AppRoute.EventList -> {
            val viewModel = container.eventListViewModel
            EventListScreen(
                viewModel = viewModel,
                onEventSelected = { appViewModel.navigate(AppRoute.EventDetails(it.id)) },
                onNavigateBack = { appViewModel.navigateBack() },
                onCreateEventClick = { appViewModel.navigate(AppRoute.CreateEvent()) },
            )
        }

        is AppRoute.CreateEvent -> {
            val viewModel = remember { container.getEventFormViewModel() }
            DisposableEffect(viewModel) { onDispose { viewModel.onCleared() } }
            CreateEventScreen(
                viewModel = viewModel,
                zoneId = route.zoneId,
                onEventCreated = { appViewModel.navigateAndReplace(AppRoute.EventDetails(it)) },
                onNavigateBack = { appViewModel.navigateBack() },
            )
        }

        is AppRoute.EventDetails -> {
            currentUserInfo?.let { user ->
                val viewModel = remember(user) { container.getEventDetailsViewModel(user) }
                DisposableEffect(viewModel) { onDispose { viewModel.onCleared() } }
                EventDetailsScreen(
                    viewModel = viewModel,
                    eventId = route.eventId,
                    onNavigateToChat = { appViewModel.navigate(AppRoute.Chat(it)) },
                    onNavigateToEditEvent = { appViewModel.navigate(AppRoute.EditEvent(it)) },
                    onNavigateToManageAttendance = {
                        appViewModel.navigate(AppRoute.ManageAttendance(it))
                    },
                    onNavigateBack = { appViewModel.navigateBack() },
                    onNavigateToMyQrCode = { userId, organizerName ->
                        appViewModel.navigate(AppRoute.MyQrCode(userId, organizerName )) },
                    onNavigateToStatusHistory = {
                        appViewModel.navigate(
                            AppRoute.EventStatusHistory(it),
                        )
                    },
                )
            }
        }

        is AppRoute.EventStatusHistory -> {
            val viewModel = remember { container.getEventStatusHistoryViewModel() }
            DisposableEffect(viewModel) { onDispose { viewModel.onCleared() } }
            EventStatusHistoryScreen(
                viewModel = viewModel,
                eventId = route.eventId,
                onNavigateBack = { appViewModel.navigateBack() },
            )
        }

        is AppRoute.EditEvent -> {
            val viewModel = remember { container.getEventFormViewModel() }
            DisposableEffect(viewModel) { onDispose { viewModel.onCleared() } }
            UpdateEventScreen(
                viewModel = viewModel,
                eventId = route.eventId,
                onNavigateBack = { appViewModel.navigateBack() },
            )
        }

        is AppRoute.Chat -> {
            currentUserInfo?.let { user ->
                val vm = remember(route.info) { container.getChatViewModel(user, route.info) }
                DisposableEffect(vm) { onDispose { vm.onCleared() } }
                ChatScreen(
                    viewModel = vm,
                    onNavigateBack = { appViewModel.navigateBack() },
                )
            }
        }

        is AppRoute.UserProfile -> {
            currentUserInfo?.let { user ->
                val viewModel = remember(user.id) { container.getUserProfileViewModel(user) }
                DisposableEffect(viewModel) { onDispose { viewModel.onCleared() } }
                UserProfileScreen(
                    viewModel = viewModel,
                    onAccountDeleted = { container.logout() },
                    onNavigateBack = { appViewModel.navigateBack() },
                    onNavigateToStats = { appViewModel.navigate(AppRoute.UserStats(user.id)) },
                    onProfileUpdated = { container.onProfileUpdated(it) },
                )
            }
        }

        is AppRoute.ManageAttendance -> {
            val vm =
                remember(route.eventId) { container.getManageAttendanceViewModel(route.eventId) }
            DisposableEffect(vm) { onDispose { vm.onCleared() } }
            ManageAttendanceScreen(
                viewModel = vm,
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
            val viewModel = remember { container.getUserStatsViewModel() }
            DisposableEffect(viewModel) { onDispose { viewModel.onCleared() } }
            UserStatsScreen(
                viewModel = viewModel,
                onEventSelected = { appViewModel.navigate(AppRoute.EventDetails(it.id)) },
                onNavigateBack = { appViewModel.navigateBack() },
            )
        }

        is AppRoute.Map -> {
            val viewModel = remember { container.getMapViewModel() }
            DisposableEffect(viewModel) { onDispose { viewModel.onCleared() } }
            MapScreen(
                viewModel = viewModel,
                onNavigateToZoneDetails = { appViewModel.navigate(AppRoute.ZoneDetails(it)) },
                onNavigateToReportZone = { lat, lon ->
                    appViewModel.navigate(AppRoute.ReportZone(lat, lon))
                },
                onNavigateBack = { appViewModel.navigateBack() },
            )
        }

        is AppRoute.ZoneDetails -> {
            val viewModel = remember { container.getZoneDetailsViewModel() }
            DisposableEffect(viewModel) { onDispose { viewModel.onCleared() } }
            ZoneDetailsScreen(
                viewModel = viewModel,
                zoneId = route.zoneId,
                onNavigateToCreateEvent = { appViewModel.navigate(AppRoute.CreateEvent(it)) },
                onNavigateToEventDetails = { appViewModel.navigate(AppRoute.EventDetails(it)) },
                onNavigateBack = { appViewModel.navigateBack() },
            )
        }

        is AppRoute.ReportZone -> {
            val viewModel = remember { container.getReportZoneViewModel() }
            DisposableEffect(viewModel) { onDispose { viewModel.onCleared() } }
            ReportZoneScreen(
                viewModel = viewModel,
                latitude = route.latitude,
                longitude = route.longitude,
                onNavigateBack = { appViewModel.navigateBack() },
            )
        }
    }
}
