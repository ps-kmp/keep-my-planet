@file:Suppress("ktlint:standard:function-naming")

package pt.isel.keepmyplanet

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import pt.isel.keepmyplanet.navigation.AppRoute
import pt.isel.keepmyplanet.ui.screens.chat.ChatScreen
import pt.isel.keepmyplanet.ui.screens.event.CreateEventScreen
import pt.isel.keepmyplanet.ui.screens.event.EventDetailsScreen
import pt.isel.keepmyplanet.ui.screens.event.EventListScreen
import pt.isel.keepmyplanet.ui.screens.event.EventScreenEvent
import pt.isel.keepmyplanet.ui.screens.home.HomeScreen
import pt.isel.keepmyplanet.ui.screens.login.LoginScreen
import pt.isel.keepmyplanet.ui.screens.register.RegisterScreen
import pt.isel.keepmyplanet.ui.screens.user.UserProfileScreen

@Composable
fun App(appViewModel: AppViewModel) {
    val currentRoute by appViewModel.currentRoute.collectAsState()
    val userSession by appViewModel.userSession.collectAsState()
    val currentUserInfo = userSession?.userInfo

    when (val route = currentRoute) {
        is AppRoute.Login -> {
            LoginScreen(
                authService = appViewModel.authService,
                onNavigateHome = { appViewModel.updateSession(it) },
                onNavigateToRegister = { appViewModel.navigate(AppRoute.Register) },
            )
        }

        is AppRoute.Register -> {
            RegisterScreen(
                userService = appViewModel.userService,
                onNavigateToLogin = { appViewModel.navigate(AppRoute.Login) },
            )
        }

        is AppRoute.Home -> {
            HomeScreen(
                user = requireNotNull(currentUserInfo) { "User must be logged in for Home route" },
                onNavigateToEventList = { appViewModel.navigate(AppRoute.EventList) },
                onNavigateToProfile = { appViewModel.navigate(AppRoute.UserProfile) },
                onLogout = { appViewModel.logout() },
            )
        }

        is AppRoute.EventList -> {
            requireNotNull(currentUserInfo) { "User must be logged in for EventList route" }
            val listState by appViewModel.eventViewModel.listUiState.collectAsState()
            EventListScreen(
                events = listState.events,
                isLoading = listState.isLoading,
                error = listState.error,
                onEventSelected = { appViewModel.navigate(AppRoute.EventDetails(it.id.value)) },
                onNavigateBack = { appViewModel.navigate(AppRoute.Home) },
                onCreateEventClick = { appViewModel.navigate(AppRoute.CreateEvent) },
            )
        }

        is AppRoute.CreateEvent -> {
            requireNotNull(currentUserInfo) { "User must be logged in for CreateEvent route" }
            val eventViewModel = appViewModel.eventViewModel

            LaunchedEffect(Unit) {
                eventViewModel.events.collect { event ->
                    when (event) {
                        is EventScreenEvent.EventCreated -> {
                            appViewModel.navigate(AppRoute.EventDetails(event.eventId))
                        }
                        else -> { /* ignore other events */ }
                    }
                }
            }

            CreateEventScreen(
                onNavigateBack = { appViewModel.navigate(AppRoute.EventList) },
                onCreateEvent = { request -> eventViewModel.createEvent(request) },
            )
        }

        is AppRoute.EventDetails -> {
            requireNotNull(currentUserInfo) { "User must be logged in for EventDetails route" }
            val eventViewModel = appViewModel.eventViewModel
            val detailsState by eventViewModel.detailsUiState.collectAsState()

            EventDetailsScreen(
                eventId = route.eventId,
                uiState = detailsState,
                onNavigateBack = { appViewModel.navigate(AppRoute.EventList) },
                onNavigateToChat = { event ->
                    appViewModel.navigate(AppRoute.Chat(event))
                },
                onLoadEventDetails = { id ->
                    eventViewModel.loadEventDetails(id)
                },
                onJoinEvent = { id ->
                    eventViewModel.joinEvent(id)
                },
            )
        }

        is AppRoute.Chat -> {
            ChatScreen(
                chatService = appViewModel.chatService,
                user = requireNotNull(currentUserInfo) { "User must be logged in for Chat route" },
                event = route.event,
                onNavigateBack = { appViewModel.navigate(AppRoute.EventList) },
            )
        }

        is AppRoute.UserProfile -> {
            UserProfileScreen(
                userService = appViewModel.userService,
                user = requireNotNull(currentUserInfo) { "User must be logged in for User route" },
                onNavigateToLogin = { appViewModel.logout() },
                onNavigateBack = { appViewModel.navigate(AppRoute.Home) },
            )
        }
    }
}
