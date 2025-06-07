package pt.isel.keepmyplanet

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import pt.isel.keepmyplanet.di.AppContainer
import pt.isel.keepmyplanet.navigation.AppRoute
import pt.isel.keepmyplanet.session.model.UserSession
import pt.isel.keepmyplanet.ui.chat.ChatViewModel
import pt.isel.keepmyplanet.ui.event.EventViewModel
import pt.isel.keepmyplanet.ui.event.model.EventScreenEvent
import pt.isel.keepmyplanet.ui.user.UserProfileViewModel

class AppViewModel(
    val container: AppContainer,
) : ViewModel() {
    val userSession: StateFlow<UserSession?> = container.userSession

    private val _currentRoute = MutableStateFlow(determineInitialRoute(userSession.value))
    val currentRoute: StateFlow<AppRoute> = _currentRoute.asStateFlow()

    private val _eventViewModel = MutableStateFlow<EventViewModel?>(null)
    val eventViewModel: StateFlow<EventViewModel?> = _eventViewModel.asStateFlow()

    private val _chatViewModel = MutableStateFlow<ChatViewModel?>(null)
    val chatViewModel: StateFlow<ChatViewModel?> = _chatViewModel.asStateFlow()

    private val _userProfileViewModel = MutableStateFlow<UserProfileViewModel?>(null)
    val userProfileViewModel: StateFlow<UserProfileViewModel?> = _userProfileViewModel.asStateFlow()

    private var eventSideEffectsJob: Job? = null

    init {
        viewModelScope.launch {
            userSession.collect { session ->
                if (session != null) {
                    if (_eventViewModel.value == null) {
                        val newVm = EventViewModel(container.eventApi, session.userInfo)
                        _eventViewModel.value = newVm
                        eventSideEffectsJob?.cancel()
                        eventSideEffectsJob =
                            viewModelScope.launch {
                                newVm.events.collect { handleEventViewModelSideEffects(it) }
                            }
                    }
                    if (_userProfileViewModel.value == null) {
                        _userProfileViewModel.value =
                            UserProfileViewModel(container.userApi, session.userInfo)
                    }
                } else {
                    eventSideEffectsJob?.cancel()
                    _eventViewModel.value = null
                    _chatViewModel.value = null
                    _userProfileViewModel.value = null
                }
                navigate(determineInitialRoute(session))
            }
        }
    }

    private fun handleEventViewModelSideEffects(event: EventScreenEvent) {
        when (event) {
            is EventScreenEvent.EventCreated -> navigate(AppRoute.EventDetails(event.eventId))
            is EventScreenEvent.NavigateBack -> {
                (currentRoute.value as? AppRoute.EditEvent)?.let {
                    navigate(AppRoute.EventDetails(it.eventId))
                }
            }

            is EventScreenEvent.ShowSnackbar -> {}
        }
    }

    fun navigate(route: AppRoute) {
        updateChatViewModel(route)
        val targetRoute = resolveRoute(route, userSession.value)
        if (_currentRoute.value != targetRoute) {
            _currentRoute.value = targetRoute
        }
    }

    private fun updateChatViewModel(route: AppRoute) {
        val session = userSession.value
        if (route is AppRoute.Chat && session != null) {
            if (_chatViewModel.value
                    ?.uiState
                    ?.value
                    ?.chatInfo
                    ?.eventId != route.info.eventId
            ) {
                _chatViewModel.value =
                    ChatViewModel(container.chatApi, session.userInfo, route.info)
            }
        } else if (currentRoute.value is AppRoute.Chat && route !is AppRoute.Chat) {
            _chatViewModel.value = null
        }
    }

    private fun resolveRoute(
        requestedRoute: AppRoute,
        session: UserSession?,
    ): AppRoute =
        if (session != null) {
            when (requestedRoute) {
                is AppRoute.Login, is AppRoute.Register -> AppRoute.Home
                else -> requestedRoute
            }
        } else {
            when (requestedRoute) {
                is AppRoute.Login, is AppRoute.Register -> requestedRoute
                else -> AppRoute.Login
            }
        }

    fun updateSession(newSession: UserSession?) = container.updateSession(newSession)

    fun logout() = container.logout()

    private fun determineInitialRoute(session: UserSession?): AppRoute =
        if (session != null) {
            AppRoute.Home
        } else {
            AppRoute.Login
        }
}
