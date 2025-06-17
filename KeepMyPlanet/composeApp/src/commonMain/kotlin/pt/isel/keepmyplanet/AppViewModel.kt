package pt.isel.keepmyplanet

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import pt.isel.keepmyplanet.navigation.AppRoute
import pt.isel.keepmyplanet.session.model.UserSession

class AppViewModel(
    val userSession: StateFlow<UserSession?>,
) : ViewModel() {
    private val _navStack = MutableStateFlow(listOf(determineInitialRoute(userSession.value)))
    val navStack: StateFlow<List<AppRoute>> = _navStack.asStateFlow()

    val currentRoute: StateFlow<AppRoute> =
        navStack
            .map { it.last() }
            .stateIn(viewModelScope, SharingStarted.Eagerly, _navStack.value.last())

    init {
        viewModelScope.launch {
            userSession.collect { session ->
                _navStack.value = listOf(determineInitialRoute(session))
            }
        }
    }

    fun navigate(route: AppRoute) {
        val resolvedRoute = resolveRoute(route, userSession.value)
        if (_navStack.value.lastOrNull() != resolvedRoute) {
            _navStack.update { it + resolvedRoute }
        }
    }

    fun navigateBack() {
        if (_navStack.value.size > 1) {
            _navStack.update { it.dropLast(1) }
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

    private fun determineInitialRoute(session: UserSession?): AppRoute =
        if (session != null) {
            AppRoute.Home
        } else {
            AppRoute.Login
        }
}
