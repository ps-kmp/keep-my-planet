package pt.isel.keepmyplanet

import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import pt.isel.keepmyplanet.data.repository.DefaultDeviceRepository
import pt.isel.keepmyplanet.domain.common.Id
import pt.isel.keepmyplanet.domain.message.ChatInfo
import pt.isel.keepmyplanet.domain.user.UserInfo
import pt.isel.keepmyplanet.domain.user.UserSession
import pt.isel.keepmyplanet.navigation.AppRoute
import pt.isel.keepmyplanet.session.SessionManager
import pt.isel.keepmyplanet.ui.app.states.AppEvent
import pt.isel.keepmyplanet.ui.app.states.AppUiState
import pt.isel.keepmyplanet.ui.base.BaseViewModel

class AppViewModel(
    private val deviceRepository: DefaultDeviceRepository,
    private val sessionManager: SessionManager,
) : BaseViewModel<AppUiState>(
        initialState =
            AppUiState().let {
                val session = sessionManager.userSession.value
                val initialRoute = determineInitialRoute(session)
                it.copy(
                    userSession = session,
                    navStack = listOf(initialRoute),
                    currentRoute = initialRoute,
                )
            },
    ) {
    init {
        viewModelScope.launch {
            sessionManager.userSession.map { it != null }.distinctUntilChanged().collect {
                val newInitialRoute = determineInitialRoute(sessionManager.userSession.value)
                setState {
                    copy(
                        userSession = sessionManager.userSession.value,
                        navStack = listOf(newInitialRoute),
                        currentRoute = newInitialRoute,
                    )
                }
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
            setState {
                copy(
                    navStack = newStack,
                    currentRoute = newStack.last(),
                )
            }
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
            setState {
                copy(
                    navStack = newStack,
                    currentRoute = newStack.last(),
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
            when (requestedRoute) {
                is AppRoute.Login, is AppRoute.Register -> requestedRoute
                else -> AppRoute.Login
            }
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

    companion object {
        private fun determineInitialRoute(session: UserSession?): AppRoute =
            if (session != null) {
                AppRoute.Home
            } else {
                AppRoute.Login
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
}
