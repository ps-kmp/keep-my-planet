package pt.isel.keepmyplanet

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import io.ktor.client.engine.HttpClientEngineFactory
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import pt.isel.keepmyplanet.di.AppContainer
import pt.isel.keepmyplanet.navigation.AppRoute
import pt.isel.keepmyplanet.session.model.UserSession

class AppViewModel(
    engine: HttpClientEngineFactory<*>,
) : ViewModel() {
    val container = AppContainer(engine)
    val userSession: StateFlow<UserSession?> = container.userSession

    private val _currentRoute = MutableStateFlow(determineInitialRoute(userSession.value))
    val currentRoute: StateFlow<AppRoute> = _currentRoute.asStateFlow()

    init {
        viewModelScope.launch {
            userSession.collect { session ->
                navigate(currentRoute.value)
            }
        }
    }

    fun navigate(route: AppRoute) {
        val targetRoute = resolveRoute(route, userSession.value)
        if (_currentRoute.value != targetRoute) {
            _currentRoute.value = targetRoute
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
