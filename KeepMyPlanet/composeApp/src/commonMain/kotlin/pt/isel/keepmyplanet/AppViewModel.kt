package pt.isel.keepmyplanet

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import pt.isel.keepmyplanet.data.model.UserSession
import pt.isel.keepmyplanet.data.service.AuthService
import pt.isel.keepmyplanet.data.service.ChatService
import pt.isel.keepmyplanet.data.service.UserService
import pt.isel.keepmyplanet.navigation.AppRoute

class AppViewModel(
    val authService: AuthService,
    val chatService: ChatService,
    val userService: UserService,
) : ViewModel() {
    private val _userSession = MutableStateFlow<UserSession?>(null)
    val userSession: StateFlow<UserSession?> = _userSession.asStateFlow()

    private val _currentRoute = MutableStateFlow(determineInitialRoute(_userSession.value))
    val currentRoute: StateFlow<AppRoute> = _currentRoute.asStateFlow()

    fun updateSession(session: UserSession?) {
        val oldId = _userSession.value?.userInfo?.id
        val newId = session?.userInfo?.id
        _userSession.value = session
        if (oldId != newId) navigate(determineInitialRoute(session))
    }

    fun navigate(route: AppRoute) {
        val targetRoute = if (_userSession.value == null) AppRoute.Login else route
        if (_currentRoute.value != targetRoute) _currentRoute.value = targetRoute
    }

    fun logout() {
        updateSession(null)
    }

    private fun determineInitialRoute(session: UserSession?): AppRoute =
        if (session != null) {
            AppRoute.Home
        } else {
            AppRoute.Login
        }
}
