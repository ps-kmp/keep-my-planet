package pt.isel.keepmyplanet

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import pt.isel.keepmyplanet.data.model.UserSession
import pt.isel.keepmyplanet.navigation.AppRoute

class AppViewModel : ViewModel() {
    private val _userSession = MutableStateFlow<UserSession?>(null)
    val userSession: StateFlow<UserSession?> = _userSession

    private val _currentRoute = MutableStateFlow<AppRoute>(AppRoute.Login)
    val currentRoute: StateFlow<AppRoute> = _currentRoute

    fun updateSession(session: UserSession?) {
        _userSession.value = session
    }

    fun navigate(route: AppRoute) {
        _currentRoute.value = route
    }
}
