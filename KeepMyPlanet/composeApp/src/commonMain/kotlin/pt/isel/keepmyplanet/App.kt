@file:Suppress("ktlint:standard:function-naming")

package pt.isel.keepmyplanet

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.CircularProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
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
import pt.isel.keepmyplanet.ui.register.RegisterScreen
import pt.isel.keepmyplanet.ui.user.UserProfileScreen

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
    when (val route = currentRoute) {
        is AppRoute.Login -> {
            val loginViewModel by appViewModel.loginViewModel.collectAsState()
            loginViewModel?.let { vm ->
                LoginScreen(
                    viewModel = vm,
                    onNavigateHome = { appViewModel.updateSession(it) },
                    onNavigateToRegister = { appViewModel.navigate(AppRoute.Register) },
                )
            }
        }

        is AppRoute.Register -> {
            val registerViewModel by appViewModel.registerViewModel.collectAsState()
            registerViewModel?.let { vm ->
                RegisterScreen(
                    viewModel = vm,
                    onNavigateToLogin = { appViewModel.navigate(AppRoute.Login) },
                )
            }
        }

        is AppRoute.Home -> {
            currentUserInfo?.let { user ->
                HomeScreen(
                    user = user,
                    onNavigateToEventList = { appViewModel.navigate(AppRoute.EventList) },
                    onNavigateToProfile = { appViewModel.navigate(AppRoute.UserProfile) },
                    onLogout = { appViewModel.logout() },
                )
            }
        }

        is AppRoute.EventList -> {
            val eventViewModel by appViewModel.eventViewModel.collectAsState()
            eventViewModel?.let { vm ->
                EventListScreen(
                    viewModel = vm,
                    onEventSelected = { appViewModel.navigate(AppRoute.EventDetails(it.id.value)) },
                    onNavigateBack = { appViewModel.navigate(AppRoute.Home) },
                    onCreateEventClick = { appViewModel.navigate(AppRoute.CreateEvent) },
                )
            }
        }

        is AppRoute.CreateEvent -> {
            val eventViewModel by appViewModel.eventViewModel.collectAsState()
            eventViewModel?.let { vm ->
                LaunchedEffect(Unit) { vm.resetFormState() }
                CreateEventScreen(
                    viewModel = vm,
                    onNavigateBack = { appViewModel.navigate(AppRoute.EventList) },
                )
            }
        }

        is AppRoute.EventDetails -> {
            currentUserInfo?.let { user ->
                val eventViewModel by appViewModel.eventViewModel.collectAsState()
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
            val eventViewModel by appViewModel.eventViewModel.collectAsState()
            eventViewModel?.let { vm ->
                val detailsState by vm.detailsUiState.collectAsState()

                LaunchedEffect(route.eventId) {
                    vm.loadEventDetails(route.eventId)
                }

                LaunchedEffect(detailsState.event) {
                    if (detailsState.event?.id?.value == route.eventId) {
                        vm.prepareFormForEdit()
                    }
                }

                if (detailsState.isLoading) {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator()
                    }
                } else {
                    detailsState.event?.let { event ->
                        UpdateEventScreen(
                            viewModel = vm,
                            onUpdateEvent = { vm.updateEvent(event.id.value) },
                            onNavigateBack = {
                                appViewModel.navigate(AppRoute.EventDetails(event.id.value))
                            },
                        )
                    }
                }
            }
        }

        is AppRoute.Chat -> {
            val chatViewModel by appViewModel.chatViewModel.collectAsState()
            chatViewModel?.let { vm ->
                ChatScreen(
                    viewModel = vm,
                    onNavigateBack = { appViewModel.navigate(AppRoute.EventList) },
                )
            }
        }

        is AppRoute.UserProfile -> {
            val userProfileViewModel by appViewModel.userProfileViewModel.collectAsState()
            userProfileViewModel?.let { vm ->
                UserProfileScreen(
                    viewModel = vm,
                    onNavigateToLogin = { appViewModel.logout() },
                    onNavigateBack = { appViewModel.navigate(AppRoute.Home) },
                )
            }
        }
    }
}
