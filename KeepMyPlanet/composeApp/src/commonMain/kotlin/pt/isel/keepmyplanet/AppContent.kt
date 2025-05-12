package pt.isel.keepmyplanet

import androidx.compose.runtime.Composable
import pt.isel.keepmyplanet.data.model.UserSession
import pt.isel.keepmyplanet.data.service.AuthService
import pt.isel.keepmyplanet.data.service.ChatService
import pt.isel.keepmyplanet.data.service.UserService
import pt.isel.keepmyplanet.navigation.AppRoute
import pt.isel.keepmyplanet.ui.screens.chat.ChatScreen
import pt.isel.keepmyplanet.ui.screens.event.EventListScreen
import pt.isel.keepmyplanet.ui.screens.home.HomeScreen
import pt.isel.keepmyplanet.ui.screens.login.LoginScreen
import pt.isel.keepmyplanet.ui.screens.user.UserProfileScreen

@Suppress("ktlint:standard:function-naming")
@Composable
fun AppContent(
    route: AppRoute,
    navigate: (AppRoute) -> Unit,
    updateSession: (UserSession?) -> Unit,
    authService: AuthService,
    chatService: ChatService,
    userService: UserService,
) {
    when (route) {
        is AppRoute.Login -> {
            LoginScreen(
                authService = authService,
                onNavigateHome = { userSession ->
                    updateSession(userSession)
                    navigate(AppRoute.Home(userSession.userInfo))
                },
            )
        }

        is AppRoute.Home -> {
            HomeScreen(
                user = route.user,
                onNavigateToEventList = {
                    navigate(AppRoute.EventList(route.user))
                },
                onNavigateToProfile = {
                    navigate(AppRoute.UserProfile(route.user))
                },
                onLogout = {
                    updateSession(null)
                    navigate(AppRoute.Login)
                },
            )
        }

        is AppRoute.EventList -> {
            EventListScreen(
                onEventSelected = { event ->
                    navigate(AppRoute.Chat(route.user, event))
                },
                onNavigateBack = {
                    navigate(AppRoute.Home(route.user))
                },
            )
        }

        is AppRoute.Chat -> {
            ChatScreen(
                chatService = chatService,
                user = route.user,
                event = route.event,
                onNavigateBack = {
                    navigate(AppRoute.EventList(route.user))
                },
            )
        }

        is AppRoute.UserProfile -> {
            UserProfileScreen(
                userService = userService,
                user = route.user,
                onNavigateToLogin = {
                    updateSession(null)
                    navigate(AppRoute.Login)
                },
                onNavigateBack = {
                    navigate(AppRoute.Home(route.user))
                },
            )
        }
    }
}
