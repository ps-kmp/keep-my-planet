package pt.isel.keepmyplanet

import kotlinx.coroutines.launch
import pt.isel.keepmyplanet.data.repository.DefaultDeviceRepository
import pt.isel.keepmyplanet.domain.common.Id
import pt.isel.keepmyplanet.domain.message.ChatInfo
import pt.isel.keepmyplanet.domain.user.UserInfo
import pt.isel.keepmyplanet.domain.user.UserSession
import pt.isel.keepmyplanet.navigation.AppRoute
import pt.isel.keepmyplanet.navigation.isRoutePublic
import pt.isel.keepmyplanet.session.SessionManager
import pt.isel.keepmyplanet.ui.app.states.AppEvent
import pt.isel.keepmyplanet.ui.app.states.AppUiState
import pt.isel.keepmyplanet.ui.base.BaseViewModel

class AppViewModel(
    private val deviceRepository: DefaultDeviceRepository,
    private val sessionManager: SessionManager,
) : BaseViewModel<AppUiState>(
        initialState =
            AppUiState().copy(
                userSession = sessionManager.userSession.value,
                navStack = listOf(AppRoute.Home),
                currentRoute = AppRoute.Home,
            ),
    ) {
    init {
        viewModelScope.launch {
            sessionManager.userSession
                .collect { newSession ->
                    val oldSession = currentState.userSession
                    val wasLoggedIn = oldSession != null
                    val isNowLoggedIn = newSession != null

                    if (wasLoggedIn && isNowLoggedIn && oldSession.token == newSession.token) {
                        setState { copy(userSession = newSession) }
                        return@collect
                    }

                    if (wasLoggedIn && !isNowLoggedIn) {
                        setState {
                            copy(
                                userSession = null,
                                navStack = listOf(AppRoute.Home),
                                currentRoute = AppRoute.Home,
                            )
                        }
                        return@collect
                    }

                    if (!wasLoggedIn && isNowLoggedIn) {
                        setState {
                            copy(
                                userSession = newSession,
                                navStack = listOf(AppRoute.Home),
                                currentRoute = AppRoute.Home,
                            )
                        }
                        return@collect
                    }
                    setState { copy(userSession = newSession) }
                }
        }
    }

    override fun handleErrorWithMessage(message: String) {
        sendEvent(AppEvent.ShowSnackbar(message))
    }

    fun navigate(route: AppRoute) {
        val resolvedRoute = resolveRoute(route, currentState.userSession)
        if (currentState.navStack.lastOrNull() != resolvedRoute) {
            val newStack = currentState.navStack + resolvedRoute
            setState { copy(navStack = newStack, currentRoute = newStack.last()) }
        }
    }

    fun navigateAndReplace(route: AppRoute) {
        val resolvedRoute = resolveRoute(route, currentState.userSession)
        if (currentState.navStack.lastOrNull() != resolvedRoute) {
            val newStack = currentState.navStack.dropLast(1) + resolvedRoute
            setState {
                copy(
                    navStack = newStack,
                    currentRoute = newStack.last(),
                )
            }
        }
    }

    fun navigateBack() {
        if (currentState.navStack.size > 1) {
            val newStack = currentState.navStack.dropLast(1)
            setState { copy(navStack = newStack, currentRoute = newStack.last()) }
        }
    }

    private fun resolveRoute(
        requestedRoute: AppRoute,
        session: UserSession?,
    ): AppRoute =
        if (session != null) { // Logged in user
            when (requestedRoute) {
                is AppRoute.Login, is AppRoute.Register -> AppRoute.Home
                else -> requestedRoute
            }
        } else { // Guest user
            if (isRoutePublic(requestedRoute)) requestedRoute else AppRoute.Login
        }

    fun updateSession(newSession: UserSession?) {
        sessionManager.saveSession(newSession)
    }

    fun logout() {
        sessionManager.clearSession()
    }

    fun onProfileUpdated(updatedUserInfo: UserInfo) {
        val currentSession = currentState.userSession
        if (currentSession != null) {
            sessionManager.saveSession(currentSession.copy(userInfo = updatedUserInfo))
        }
    }

    fun handleNotificationNavigation(eventId: String) {
        val eventIdAsUInt = eventId.toUIntOrNull() ?: return
        val eventIdDomain = Id(eventIdAsUInt)
        val chatInfo = ChatInfo(eventIdDomain, null)

        if (currentState.userSession != null) {
            navigate(AppRoute.Chat(chatInfo))
        }
    }

    fun registerDeviceToken(token: String) {
        viewModelScope.launch {
            deviceRepository.registerDevice(token, "ANDROID")
        }
    }

    fun navigateToHome() {
        val homeRoute = AppRoute.Home
        if (currentState.navStack.size > 1 || currentState.currentRoute != homeRoute) {
            setState { copy(navStack = listOf(homeRoute), currentRoute = homeRoute) }
        }
    }
}
