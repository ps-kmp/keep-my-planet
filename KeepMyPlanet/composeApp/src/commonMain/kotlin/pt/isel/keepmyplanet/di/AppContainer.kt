package pt.isel.keepmyplanet.di

import io.ktor.client.HttpClient
import kotlinx.coroutines.flow.StateFlow
import pt.isel.keepmyplanet.AppViewModel
import pt.isel.keepmyplanet.data.api.AuthApi
import pt.isel.keepmyplanet.data.api.ChatApi
import pt.isel.keepmyplanet.data.api.EventApi
import pt.isel.keepmyplanet.data.api.UserApi
import pt.isel.keepmyplanet.data.api.ZoneApi
import pt.isel.keepmyplanet.data.http.createHttpClient
import pt.isel.keepmyplanet.domain.common.Id
import pt.isel.keepmyplanet.dto.message.ChatInfo
import pt.isel.keepmyplanet.dto.user.UserInfo
import pt.isel.keepmyplanet.dto.user.UserSession
import pt.isel.keepmyplanet.session.SessionManager
import pt.isel.keepmyplanet.ui.chat.ChatViewModel
import pt.isel.keepmyplanet.ui.event.attendance.ManageAttendanceViewModel
import pt.isel.keepmyplanet.ui.event.details.EventDetailsViewModel
import pt.isel.keepmyplanet.ui.event.details.history.EventStatusHistoryViewModel
import pt.isel.keepmyplanet.ui.event.forms.EventFormViewModel
import pt.isel.keepmyplanet.ui.event.list.EventListViewModel
import pt.isel.keepmyplanet.ui.login.LoginViewModel
import pt.isel.keepmyplanet.ui.map.MapViewModel
import pt.isel.keepmyplanet.ui.register.RegisterViewModel
import pt.isel.keepmyplanet.ui.user.profile.UserProfileViewModel
import pt.isel.keepmyplanet.ui.user.stats.UserStatsViewModel
import pt.isel.keepmyplanet.ui.zone.details.ZoneDetailsViewModel
import pt.isel.keepmyplanet.ui.zone.report.ReportZoneViewModel

class AppContainer {
    private val sessionManager = SessionManager()
    val userSession: StateFlow<UserSession?> = sessionManager.userSession

    fun updateSession(newSession: UserSession?) {
        sessionManager.saveSession(newSession)
    }

    fun logout() {
        sessionManager.clearSession()
    }

    fun onProfileUpdated(updatedUserInfo: UserInfo) {
        val currentSession = userSession.value
        if (currentSession != null) {
            sessionManager.saveSession(currentSession.copy(userInfo = updatedUserInfo))
        }
    }

    private val httpClient: HttpClient by lazy {
        createHttpClient(sessionManager)
    }

    private val authApi: AuthApi by lazy { AuthApi(httpClient) }
    private val userApi: UserApi by lazy { UserApi(httpClient) }
    private val eventApi: EventApi by lazy { EventApi(httpClient) }
    private val chatApi: ChatApi by lazy { ChatApi(httpClient) }
    private val zoneApi: ZoneApi by lazy { ZoneApi(httpClient) }

    val appViewModel: AppViewModel by lazy { AppViewModel(userSession) }

    fun getLoginViewModel() = LoginViewModel(authApi)

    fun getRegisterViewModel() = RegisterViewModel(userApi)

    val eventListViewModel: EventListViewModel by lazy { EventListViewModel(eventApi) }

    fun getEventDetailsViewModel(user: UserInfo) = EventDetailsViewModel(eventApi, user)

    fun getEventStatusHistoryViewModel() = EventStatusHistoryViewModel(eventApi)

    fun getEventFormViewModel() = EventFormViewModel(eventApi)

    fun getMapViewModel() = MapViewModel(zoneApi)

    fun getZoneDetailsViewModel() = ZoneDetailsViewModel(zoneApi)

    fun getReportZoneViewModel() = ReportZoneViewModel(zoneApi)

    fun getUserProfileViewModel(user: UserInfo) = UserProfileViewModel(userApi, user)

    fun getChatViewModel(
        user: UserInfo,
        chat: ChatInfo,
    ): ChatViewModel = ChatViewModel(chatApi, user, chat)

    fun getManageAttendanceViewModel(eventId: Id) = ManageAttendanceViewModel(eventApi, eventId)

    fun getUserStatsViewModel() = UserStatsViewModel(eventApi)
}
