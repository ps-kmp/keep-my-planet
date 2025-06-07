@file:Suppress("ktlint:standard:function-naming")

package pt.isel.keepmyplanet

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import kotlinx.datetime.LocalDateTime
import pt.isel.keepmyplanet.data.model.EventInfo
import pt.isel.keepmyplanet.domain.common.Description
import pt.isel.keepmyplanet.domain.common.Id
import pt.isel.keepmyplanet.domain.event.EventStatus
import pt.isel.keepmyplanet.domain.event.Period
import pt.isel.keepmyplanet.domain.event.Title
import pt.isel.keepmyplanet.navigation.AppRoute
import pt.isel.keepmyplanet.ui.screens.chat.ChatScreen
import pt.isel.keepmyplanet.ui.screens.event.CreateEventScreen
import pt.isel.keepmyplanet.ui.screens.event.EventDetailsScreen
import pt.isel.keepmyplanet.ui.screens.event.EventListScreen
import pt.isel.keepmyplanet.ui.screens.event.EventScreenEvent
import pt.isel.keepmyplanet.ui.screens.event.UpdateEventScreen
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
                authHttpClient = appViewModel.authHttpClient,
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
            currentUserInfo?.let { user ->
                HomeScreen(
                    user = user,
                    onNavigateToEventList = { appViewModel.navigate(AppRoute.EventList) },
                    onNavigateToProfile = { appViewModel.navigate(AppRoute.UserProfile) },
                    onLogout = { appViewModel.logout() },
                )
            } ?: LaunchedEffect(Unit) { appViewModel.logout() }
        }

        is AppRoute.EventList -> {
            currentUserInfo?.let {
                appViewModel.eventViewModel?.let { eventViewModel ->
                    val listState by eventViewModel.listUiState.collectAsState()
                    EventListScreen(
                        uiState = listState,
                        onEventSelected = { appViewModel.navigate(AppRoute.EventDetails(it.id.value)) },
                        onNavigateBack = { appViewModel.navigate(AppRoute.Home) },
                        onCreateEventClick = { appViewModel.navigate(AppRoute.CreateEvent) },
                        onLoadNextPage = eventViewModel::loadNextPage,
                        onLoadPreviousPage = eventViewModel::loadPreviousPage,
                        onChangeLimit = eventViewModel::changeLimit,
                    )
                }
            } ?: LaunchedEffect(Unit) { appViewModel.logout() }
        }

        is AppRoute.CreateEvent -> {
            currentUserInfo?.let {
                appViewModel.eventViewModel?.let { eventViewModel ->
                    LaunchedEffect(Unit) {
                        eventViewModel.events.collect { event ->
                            when (event) {
                                is EventScreenEvent.EventCreated -> {
                                    appViewModel.navigate(AppRoute.EventDetails(event.eventId))
                                }

                                else -> { // ignore other events
                                }
                            }
                        }
                    }

                    CreateEventScreen(
                        onNavigateBack = { appViewModel.navigate(AppRoute.EventList) },
                        onCreateEvent = { request -> eventViewModel.createEvent(request) },
                    )
                }
            } ?: LaunchedEffect(Unit) { appViewModel.logout() }
        }

        is AppRoute.EventDetails -> {
            currentUserInfo?.let { user ->
                appViewModel.eventViewModel?.let { eventViewModel ->
                    val detailsState by eventViewModel.detailsUiState.collectAsState()

                    EventDetailsScreen(
                        userId = user.id,
                        eventId = route.eventId,
                        uiState = detailsState,
                        onNavigateBack = { appViewModel.navigate(AppRoute.EventList) },
                        onNavigateToChat = { event -> appViewModel.navigate(AppRoute.Chat(event)) },
                        onLoadEventDetails = { id -> eventViewModel.loadEventDetails(id) },
                        onJoinEvent = { id -> eventViewModel.joinEvent(id) },
                        onNavigateToEditEvent = { appViewModel.navigate(AppRoute.EditEvent(route.eventId)) },
                    )
                }
            } ?: LaunchedEffect(Unit) { appViewModel.logout() }
        }

        is AppRoute.EditEvent -> {
            currentUserInfo?.let {
                appViewModel.eventViewModel?.let { eventViewModel ->
                    val detailsState by eventViewModel.detailsUiState.collectAsState()

                    LaunchedEffect(eventViewModel) {
                        eventViewModel.events.collect { event ->
                            if (event is EventScreenEvent.NavigateBack) {
                                appViewModel.navigate(AppRoute.EventDetails(route.eventId))
                            }
                        }
                    }

                    detailsState.event?.let { event ->
                        UpdateEventScreen(
                            event =
                                EventInfo(
                                    id = Id(event.id),
                                    title = Title(event.title),
                                    description = Description(event.description),
                                    period =
                                        Period(
                                            start = LocalDateTime.parse(event.startDate),
                                            end = event.endDate?.let { LocalDateTime.parse(it) },
                                        ),
                                    status = EventStatus.valueOf(event.status.uppercase()),
                                    maxParticipants = event.maxParticipants,
                                ),
                            onNavigateBack = { appViewModel.navigate(AppRoute.EventDetails(event.id)) },
                            onUpdateEvent = { request ->
                                eventViewModel.updateEvent(event.id, request)
                            },
                        )
                    }
                }
            } ?: LaunchedEffect(Unit) { appViewModel.logout() }
        }

        is AppRoute.Chat -> {
            currentUserInfo?.let { user ->
                ChatScreen(
                    chatHttpClient = appViewModel.chatHttpClient,
                    user = user,
                    event = route.event,
                    onNavigateBack = { appViewModel.navigate(AppRoute.EventList) },
                )
            } ?: LaunchedEffect(Unit) { appViewModel.logout() }
        }

        is AppRoute.UserProfile -> {
            currentUserInfo?.let { user ->
                UserProfileScreen(
                    userService = appViewModel.userService,
                    user = user,
                    onNavigateToLogin = { appViewModel.logout() },
                    onNavigateBack = { appViewModel.navigate(AppRoute.Home) },
                )
            } ?: LaunchedEffect(Unit) { appViewModel.logout() }
        }
    }
}
