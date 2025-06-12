package pt.isel.keepmyplanet.di

import io.ktor.client.HttpClient
import io.ktor.client.engine.HttpClientEngineFactory
import kotlinx.coroutines.flow.StateFlow
import pt.isel.keepmyplanet.data.api.AuthApi
import pt.isel.keepmyplanet.data.api.ChatApi
import pt.isel.keepmyplanet.data.api.EventApi
import pt.isel.keepmyplanet.data.api.UserApi
import pt.isel.keepmyplanet.data.api.ZoneApi
import pt.isel.keepmyplanet.data.http.createHttpClient
import pt.isel.keepmyplanet.session.SessionManager
import pt.isel.keepmyplanet.session.model.UserSession

class AppContainer(
    engine: HttpClientEngineFactory<*>,
) {
    private val sessionManager = SessionManager()

    val userSession: StateFlow<UserSession?> = sessionManager.userSession

    fun updateSession(newSession: UserSession?) {
        sessionManager.saveSession(newSession)
    }

    fun logout() {
        sessionManager.clearSession()
    }

    private val httpClient: HttpClient by lazy {
        createHttpClient(engine, sessionManager)
    }

    val authApi: AuthApi by lazy { AuthApi(httpClient) }
    val userApi: UserApi by lazy { UserApi(httpClient) }
    val eventApi: EventApi by lazy { EventApi(httpClient) }
    val chatApi: ChatApi by lazy { ChatApi(httpClient) }
    val zoneApi: ZoneApi by lazy { ZoneApi(httpClient) }
}
