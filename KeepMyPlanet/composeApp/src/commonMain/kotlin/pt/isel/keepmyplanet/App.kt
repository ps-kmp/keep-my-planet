@file:Suppress("ktlint:standard:function-naming")

package pt.isel.keepmyplanet

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.CircularProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import pt.isel.keepmyplanet.navigation.AppRoute
import pt.isel.keepmyplanet.ui.chat.ChatScreen
import pt.isel.keepmyplanet.ui.event.create.CreateEventScreen
import pt.isel.keepmyplanet.ui.event.details.EventDetailsScreen
import pt.isel.keepmyplanet.ui.event.list.EventListScreen
import pt.isel.keepmyplanet.ui.event.update.UpdateEventScreen
import pt.isel.keepmyplanet.ui.home.HomeScreen
import pt.isel.keepmyplanet.ui.login.LoginScreen
import pt.isel.keepmyplanet.ui.map.MapScreen
import pt.isel.keepmyplanet.ui.register.RegisterScreen
import pt.isel.keepmyplanet.ui.user.UserProfileScreen
import pt.isel.keepmyplanet.ui.zone.ReportZoneScreen
import pt.isel.keepmyplanet.ui.zone.ZoneDetailsScreen

@Composable
fun App(appViewModel: AppViewModel) {
    val currentRoute by appViewModel.currentRoute.collectAsState()
    val userSession by appViewModel.userSession.collectAsState()

    val isProtectedRoute = currentRoute !is AppRoute.Login && currentRoute !is AppRoute.Register
    if (isProtectedRoute && userSession == null) {
        LaunchedEffect(currentRoute) {
            appViewModel.navigate(AppRoute.Login)
        }
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator()
        }
        return
    }

    val container = appViewModel.container
    val currentUserInfo = userSession?.userInfo

    val eventListState = rememberLazyListState()
    val loginViewModel = remember { container.createLoginViewModel() }
    val registerViewModel = remember { container.createRegisterViewModel() }
    val mapViewModel = remember { container.createMapViewModel() }
    val eventListViewModel = remember { container.createEventListViewModel() }
    val eventDetailsViewModel = remember { container.createEventDetailsViewModel() }
    val eventFormViewModel = remember { container.createEventFormViewModel() }
    val zoneViewModel = remember { container.createZoneViewModel() }
    val userProfileViewModel =
        currentUserInfo?.let { user ->
            remember(user.id) { container.createUserProfileViewModel(user) }
        }

    when (val route = currentRoute) {
        is AppRoute.Login -> {
            LoginScreen(
                viewModel = loginViewModel,
                onNavigateHome = { appViewModel.updateSession(it) },
                onNavigateToRegister = { appViewModel.navigate(AppRoute.Register) },
            )
        }

        is AppRoute.Register -> {
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
                    onLogout = { appViewModel.logout() },
                )
            }
        }

        is AppRoute.EventList -> {
            EventListScreen(
                viewModel = eventListViewModel,
                listState = eventListState,
                onEventSelected = { appViewModel.navigate(AppRoute.EventDetails(it.id)) },
                onNavigateBack = { appViewModel.navigateBack() },
                onCreateEventClick = { appViewModel.navigate(AppRoute.CreateEvent()) },
            )
        }

        is AppRoute.CreateEvent -> {
            CreateEventScreen(
                viewModel = eventFormViewModel,
                zoneId = route.zoneId,
                onEventCreated = {
                    eventListViewModel.refreshEvents()
                    appViewModel.navigate(AppRoute.EventDetails(it))
                },
                onNavigateBack = { appViewModel.navigateBack() },
            )
        }

        is AppRoute.EventDetails -> {
            currentUserInfo?.let { user ->
                EventDetailsScreen(
                    viewModel = eventDetailsViewModel,
                    userId = user.id.value,
                    eventId = route.eventId,
                    onNavigateBack = {
                        eventListViewModel.refreshEvents()
                        appViewModel.navigateBack()
                    },
                    onNavigateToChat = { appViewModel.navigate(AppRoute.Chat(it)) },
                    onNavigateToEditEvent = { appViewModel.navigate(AppRoute.EditEvent(it)) },
                )
            }
        }

        is AppRoute.EditEvent -> {
            UpdateEventScreen(
                viewModel = eventFormViewModel,
                eventId = route.eventId,
                onNavigateBack = { appViewModel.navigateBack() },
            )
        }

        is AppRoute.Chat -> {
            currentUserInfo?.let { user ->
                val vm = remember(route.info) { container.createChatViewModel(user, route.info) }
                ChatScreen(
                    viewModel = vm,
                    onNavigateBack = { appViewModel.navigateBack() },
                )
            }
        }

        is AppRoute.UserProfile -> {
            userProfileViewModel?.let {
                UserProfileScreen(
                    viewModel = it,
                    onNavigateToLogin = { appViewModel.logout() },
                    onNavigateBack = { appViewModel.navigateBack() },
                )
            }
        }

        is AppRoute.Map -> {
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
            ZoneDetailsScreen(
                viewModel = zoneViewModel,
                zoneId = route.zoneId,
                onNavigateBack = { appViewModel.navigateBack() },
                onNavigateToCreateEvent = { appViewModel.navigate(AppRoute.CreateEvent(it)) },
                onNavigateToEventDetails = { appViewModel.navigate(AppRoute.EventDetails(it)) },
            )
        }

        is AppRoute.ReportZone -> {
            ReportZoneScreen(
                viewModel = zoneViewModel,
                latitude = route.latitude,
                longitude = route.longitude,
                onNavigateBack = {
                    mapViewModel.loadZones()
                    appViewModel.navigateBack()
                },
            )
        }
    }
}
