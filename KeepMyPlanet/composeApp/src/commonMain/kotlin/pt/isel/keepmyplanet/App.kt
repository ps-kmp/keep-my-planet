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
import pt.isel.keepmyplanet.ui.chat.ChatViewModel
import pt.isel.keepmyplanet.ui.event.EventViewModel
import pt.isel.keepmyplanet.ui.event.create.CreateEventScreen
import pt.isel.keepmyplanet.ui.event.details.EventDetailsScreen
import pt.isel.keepmyplanet.ui.event.list.EventListScreen
import pt.isel.keepmyplanet.ui.event.update.UpdateEventScreen
import pt.isel.keepmyplanet.ui.home.HomeScreen
import pt.isel.keepmyplanet.ui.login.LoginScreen
import pt.isel.keepmyplanet.ui.login.LoginViewModel
import pt.isel.keepmyplanet.ui.map.MapScreen
import pt.isel.keepmyplanet.ui.map.MapViewModel
import pt.isel.keepmyplanet.ui.register.RegisterScreen
import pt.isel.keepmyplanet.ui.register.RegisterViewModel
import pt.isel.keepmyplanet.ui.user.UserProfileScreen
import pt.isel.keepmyplanet.ui.user.UserProfileViewModel

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

    val currentUserInfo = userSession?.userInfo
    val eventViewModel =
        currentUserInfo?.let { user ->
            remember(user.id) { EventViewModel(appViewModel.container.eventApi, user) }
        }
    val userProfileViewModel =
        currentUserInfo?.let { user ->
            remember(user.id) { UserProfileViewModel(appViewModel.container.userApi, user) }
        }

    val eventListState = rememberLazyListState()

    when (val route = currentRoute) {
        is AppRoute.Login -> {
            val loginViewModel = remember { LoginViewModel(appViewModel.container.authApi) }
            LoginScreen(
                viewModel = loginViewModel,
                onNavigateHome = { appViewModel.updateSession(it) },
                onNavigateToRegister = { appViewModel.navigate(AppRoute.Register) },
            )
        }

        is AppRoute.Register -> {
            val registerViewModel = remember { RegisterViewModel(appViewModel.container.userApi) }
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
            eventViewModel?.let { vm ->
                EventListScreen(
                    viewModel = vm,
                    listState = eventListState,
                    onEventSelected = { appViewModel.navigate(AppRoute.EventDetails(it.id.value)) },
                    onNavigateBack = { appViewModel.navigate(AppRoute.Home) },
                    onCreateEventClick = { appViewModel.navigate(AppRoute.CreateEvent) },
                )
            }
        }

        is AppRoute.CreateEvent -> {
            eventViewModel?.let { vm ->
                CreateEventScreen(
                    viewModel = vm,
                    onEventCreated = { appViewModel.navigate(AppRoute.EventDetails(it)) },
                    onNavigateBack = { appViewModel.navigate(AppRoute.EventList) },
                )
            }
        }

        is AppRoute.EventDetails -> {
            currentUserInfo?.let { user ->
                eventViewModel?.let { vm ->
                    EventDetailsScreen(
                        viewModel = vm,
                        userId = user.id.value,
                        eventId = route.eventId,
                        onNavigateBack = { appViewModel.navigate(AppRoute.EventList) },
                        onNavigateToChat = { appViewModel.navigate(AppRoute.Chat(it)) },
                        onNavigateToEditEvent = {
                            appViewModel.navigate(AppRoute.EditEvent(route.eventId))
                        },
                    )
                }
            }
        }

        is AppRoute.EditEvent -> {
            currentUserInfo?.let { user ->
                eventViewModel?.let { vm ->
                    UpdateEventScreen(
                        viewModel = vm,
                        eventId = route.eventId,
                        onNavigateBack = {
                            appViewModel.navigate(AppRoute.EventDetails(route.eventId))
                        },
                    )
                }
            }
        }

        is AppRoute.Chat -> {
            currentUserInfo?.let { user ->
                val chatViewModel =
                    remember(route.info) {
                        ChatViewModel(appViewModel.container.chatApi, user, route.info)
                    }
                ChatScreen(
                    viewModel = chatViewModel,
                    onNavigateBack = { appViewModel.navigate(AppRoute.EventList) },
                )
            }
        }

        is AppRoute.UserProfile -> {
            userProfileViewModel?.let {
                UserProfileScreen(
                    viewModel = it,
                    onNavigateToLogin = { appViewModel.logout() },
                    onNavigateBack = { appViewModel.navigate(AppRoute.Home) },
                )
            }
        }

        is AppRoute.Map -> {
            val mapViewModel = remember { MapViewModel(appViewModel.container.zoneApi) }
            MapScreen(
                viewModel = mapViewModel,
                onNavigateToZoneDetails = {
                    // appViewModel.navigate(AppRoute.ZoneDetails(it))
                    appViewModel.navigate(AppRoute.Home)
                },
                onNavigateBack = { appViewModel.navigate(AppRoute.Home) },
            )
        }

        is AppRoute.ZoneDetails -> {
            // TODO: ZoneDetailsScreen
        }
    }
}
