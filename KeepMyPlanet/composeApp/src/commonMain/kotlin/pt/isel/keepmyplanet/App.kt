@file:Suppress("ktlint:standard:function-naming")

package pt.isel.keepmyplanet

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import pt.isel.keepmyplanet.navigation.AppRoute
import pt.isel.keepmyplanet.ui.screens.chat.ChatScreen
import pt.isel.keepmyplanet.ui.screens.event.EventDetailsScreen
import pt.isel.keepmyplanet.ui.screens.event.EventListScreen
import pt.isel.keepmyplanet.ui.screens.home.HomeScreen
import pt.isel.keepmyplanet.ui.screens.login.LoginScreen
import pt.isel.keepmyplanet.ui.screens.user.UserProfileScreen

@Composable
fun App(appViewModel: AppViewModel) {
    val currentRoute by appViewModel.currentRoute.collectAsState()
    val userSession by appViewModel.userSession.collectAsState()
    val currentUserInfo = userSession?.userInfo

    when (currentRoute) {
        is AppRoute.Login -> {
            LoginScreen(
                authService = appViewModel.authService,
                onNavigateHome = { appViewModel.updateSession(it) },
            )
        }

        is AppRoute.Home -> {
            requireNotNull(currentUserInfo) { "User must be logged in for Home route" }
            HomeScreen(
                user = currentUserInfo,
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
                onEventSelected = { appViewModel.navigate(AppRoute.EventDetails(it.id)) },
                onNavigateBack = { appViewModel.navigate(AppRoute.Home) },
            )
        }

        is AppRoute.EventDetails -> {
            requireNotNull(currentUserInfo) { "User must be logged in for EventDetails route" }
            val eventViewModel = appViewModel.eventViewModel
            val detailsState by eventViewModel.detailsUiState.collectAsState()

            EventDetailsScreen(
                eventId = (currentRoute as AppRoute.EventDetails).eventId,
                uiState = detailsState,
                onNavigateBack = { appViewModel.navigate(AppRoute.EventList) },
                onNavigateToChat = { event ->
                    appViewModel.navigate(AppRoute.Chat(event))
                },
                onLoadEventDetails = { id ->
                    eventViewModel.loadEventDetails(id)
                },
            )
        }

        is AppRoute.Chat -> {
            requireNotNull(currentUserInfo) { "User must be logged in for Chat route" }
            ChatScreen(
                chatService = appViewModel.chatService,
                user = currentUserInfo,
                event = (currentRoute as AppRoute.Chat).event,
                onNavigateBack = { appViewModel.navigate(AppRoute.EventList) },
            )
        }

        is AppRoute.UserProfile -> {
            requireNotNull(currentUserInfo) { "User must be logged in for UserProfile route" }
            UserProfileScreen(
                userService = appViewModel.userService,
                user = currentUserInfo,
                onNavigateToLogin = { appViewModel.logout() },
                onNavigateBack = { appViewModel.navigate(AppRoute.Home) },
            )
        }
    }
}
