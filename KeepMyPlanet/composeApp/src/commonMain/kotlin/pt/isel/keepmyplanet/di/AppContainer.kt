package pt.isel.keepmyplanet.di

import io.ktor.client.HttpClient
import io.ktor.client.engine.HttpClientEngineFactory
import kotlinx.coroutines.flow.StateFlow
import pt.isel.keepmyplanet.AppViewModel
import pt.isel.keepmyplanet.data.api.AuthApi
import pt.isel.keepmyplanet.data.api.ChatApi
import pt.isel.keepmyplanet.data.api.EventApi
import pt.isel.keepmyplanet.data.api.UserApi
import pt.isel.keepmyplanet.data.api.ZoneApi
import pt.isel.keepmyplanet.data.http.createHttpClient
import pt.isel.keepmyplanet.session.SessionManager
import pt.isel.keepmyplanet.session.model.UserSession
import pt.isel.keepmyplanet.ui.chat.ChatViewModel
import pt.isel.keepmyplanet.ui.chat.model.ChatInfo
import pt.isel.keepmyplanet.ui.event.details.EventDetailsViewModel
import pt.isel.keepmyplanet.ui.event.forms.EventFormViewModel
import pt.isel.keepmyplanet.ui.event.list.EventListViewModel
import pt.isel.keepmyplanet.ui.login.LoginViewModel
import pt.isel.keepmyplanet.ui.map.MapViewModel
import pt.isel.keepmyplanet.ui.register.RegisterViewModel
import pt.isel.keepmyplanet.ui.user.UserProfileViewModel
import pt.isel.keepmyplanet.ui.user.model.UserInfo
import pt.isel.keepmyplanet.ui.zone.ReportZoneViewModel
import pt.isel.keepmyplanet.ui.zone.details.ZoneDetailsViewModel

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

    private val authApi: AuthApi by lazy { AuthApi(httpClient) }
    private val userApi: UserApi by lazy { UserApi(httpClient) }
    private val eventApi: EventApi by lazy { EventApi(httpClient) }
    private val chatApi: ChatApi by lazy { ChatApi(httpClient) }
    private val zoneApi: ZoneApi by lazy { ZoneApi(httpClient) }

    val appViewModel: AppViewModel by lazy { AppViewModel(userSession) }

    fun createLoginViewModel() = LoginViewModel(authApi, ::updateSession)

    fun createRegisterViewModel() = RegisterViewModel(userApi)

    fun createEventListViewModel() = EventListViewModel(eventApi)

    fun createEventDetailsViewModel() = EventDetailsViewModel(eventApi)

    fun createEventFormViewModel() = EventFormViewModel(eventApi)

    fun createMapViewModel() = MapViewModel(zoneApi)

    fun createZoneDetailsViewModel() = ZoneDetailsViewModel(zoneApi)

    fun createReportZoneViewModel() = ReportZoneViewModel(zoneApi)

    fun createUserProfileViewModel(user: UserInfo) = UserProfileViewModel(userApi, user)

    fun createChatViewModel(
        user: UserInfo,
        chatInfo: ChatInfo,
    ) = ChatViewModel(chatApi, user, chatInfo)
}
