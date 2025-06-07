package pt.isel.keepmyplanet

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import io.ktor.client.HttpClient
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import pt.isel.keepmyplanet.data.api.createHttpClient
import pt.isel.keepmyplanet.data.model.UserSession
import pt.isel.keepmyplanet.data.service.AuthHttpClient
import pt.isel.keepmyplanet.data.service.ChatHttpClient
import pt.isel.keepmyplanet.data.service.EventHttpClient
import pt.isel.keepmyplanet.data.service.UserHttpClient
import pt.isel.keepmyplanet.navigation.AppRoute
import pt.isel.keepmyplanet.ui.screens.event.EventViewModel

class AppViewModel : ViewModel() {
    private val _userSession = MutableStateFlow<UserSession?>(null)
    val userSession: StateFlow<UserSession?> = _userSession.asStateFlow()

    private val httpClient: HttpClient = createHttpClient { userSession.value?.token }

    val authHttpClient: AuthHttpClient = AuthHttpClient(httpClient)
    val chatHttpClient: ChatHttpClient = ChatHttpClient(httpClient)
    val userService: UserHttpClient = UserHttpClient(httpClient)
    val eventHttpClient: EventHttpClient = EventHttpClient(httpClient)

    private val _currentRoute = MutableStateFlow(determineInitialRoute(null))
    val currentRoute: StateFlow<AppRoute> = _currentRoute.asStateFlow()

    var eventViewModel: EventViewModel? = null
        private set

    init {
        viewModelScope.launch {
            userSession.collect { session ->
                eventViewModel =
                    if (session != null) EventViewModel(eventHttpClient, session.userInfo) else null
                val newRoute = determineInitialRoute(session)
                if (_currentRoute.value != newRoute) {
                    _currentRoute.value = newRoute
                }
            }
        }
    }

    fun updateSession(newSession: UserSession?) {
        _userSession.value = newSession
    }

    fun navigate(route: AppRoute) {
        val currentSession = _userSession.value
        val targetRoute =
            if (currentSession != null) {
                when (route) {
                    is AppRoute.Login, is AppRoute.Register -> AppRoute.Home
                    else -> route
                }
            } else {
                when (route) {
                    is AppRoute.Login, is AppRoute.Register -> route
                    else -> AppRoute.Login
                }
            }
        if (_currentRoute.value != targetRoute) {
            _currentRoute.value = targetRoute
        }
    }

    fun logout() {
        updateSession(null)
        _currentRoute.value = AppRoute.Login
    }

    private fun determineInitialRoute(session: UserSession?): AppRoute =
        if (session != null) {
            AppRoute.Home
        } else {
            AppRoute.Login
        }

    override fun onCleared() {
        super.onCleared()
        httpClient.close()
    }
}
