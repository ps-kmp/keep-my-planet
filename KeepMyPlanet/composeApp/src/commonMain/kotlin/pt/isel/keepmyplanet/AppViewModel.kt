package pt.isel.keepmyplanet

import kotlinx.coroutines.launch
import pt.isel.keepmyplanet.data.repository.DeviceApiRepository
import pt.isel.keepmyplanet.domain.common.Id
import pt.isel.keepmyplanet.domain.user.UserInfo
import pt.isel.keepmyplanet.domain.user.UserSession
import pt.isel.keepmyplanet.navigation.AppRoute
import pt.isel.keepmyplanet.navigation.NavDirection
import pt.isel.keepmyplanet.navigation.isRoutePublic
import pt.isel.keepmyplanet.session.SessionManager
import pt.isel.keepmyplanet.ui.app.states.AppEvent
import pt.isel.keepmyplanet.ui.app.states.AppUiState
import pt.isel.keepmyplanet.ui.base.BaseViewModel

class AppViewModel(
    private val deviceRepository: DeviceApiRepository,
    private val sessionManager: SessionManager,
) : BaseViewModel<AppUiState>(
        initialState =
            AppUiState().copy(
                userSession = sessionManager.userSession.value,
                navStack = listOf(AppRoute.Home),
                currentRoute = AppRoute.Home,
                navDirection = NavDirection.REPLACE,
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
                                navDirection = NavDirection.REPLACE,
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
                                navDirection = NavDirection.REPLACE,
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
        if (route is AppRoute.Downloads && !isWasmPlatform) return
        val resolvedRoute = resolveRoute(route, currentState.userSession)
        if (currentState.navStack.lastOrNull() != resolvedRoute) {
            val newStack = currentState.navStack + resolvedRoute
            setState {
                copy(
                    navStack = newStack,
                    currentRoute = newStack.last(),
                    navDirection = NavDirection.FORWARD,
                )
            }
        }
    }

    fun navigateAndReplace(route: AppRoute) {
        if (route is AppRoute.Downloads && !isWasmPlatform) return
        val resolvedRoute = resolveRoute(route, currentState.userSession)
        if (currentState.navStack.lastOrNull() != resolvedRoute) {
            val newStack = currentState.navStack.dropLast(1) + resolvedRoute
            setState {
                copy(
                    navStack = newStack,
                    currentRoute = newStack.last(),
                    navDirection = NavDirection.REPLACE,
                )
            }
        }
    }

    fun navigateBack() {
        if (currentState.navStack.size > 1) {
            val newStack = currentState.navStack.dropLast(1)
            setState {
                copy(
                    navStack = newStack,
                    currentRoute = newStack.last(),
                    navDirection = NavDirection.BACKWARD,
                )
            }
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
        val newStack = listOf(AppRoute.Home, AppRoute.EventDetails(eventIdDomain))

        setState {
            copy(
                navStack = newStack,
                currentRoute = newStack.last(),
                navDirection = NavDirection.REPLACE,
            )
        }
    }

    fun registerDeviceToken(token: String) {
        viewModelScope.launch {
            deviceRepository.registerDevice(token, "ANDROID").onFailure {
                handleErrorWithMessage(getErrorMessage("Failed to register for notifications", it))
            }
        }
    }

    fun navigateToHome() {
        val homeRoute = AppRoute.Home
        if (currentState.currentRoute != homeRoute) {
            val direction =
                if (currentState.navStack.size > 1) NavDirection.BACKWARD else NavDirection.REPLACE
            setState {
                copy(
                    navStack = listOf(homeRoute),
                    currentRoute = homeRoute,
                    navDirection = direction,
                )
            }
        }
    }
}
