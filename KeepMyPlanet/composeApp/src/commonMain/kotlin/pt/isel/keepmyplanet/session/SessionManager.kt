package pt.isel.keepmyplanet.session

import com.russhwolf.settings.Settings
import com.russhwolf.settings.set
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.serialization.SerializationException
import kotlinx.serialization.json.Json
import pt.isel.keepmyplanet.domain.user.UserSession

class SessionManager {
    private val settings: Settings = Settings()

    private val _userSession = MutableStateFlow(loadSession())
    val userSession = _userSession.asStateFlow()

    fun saveSession(session: UserSession?) {
        _userSession.value = session
        if (session != null) {
            try {
                val sessionJson = Json.encodeToString(session)
                settings[USER_SESSION_KEY] = sessionJson
            } catch (_: SerializationException) {
            }
        } else {
            settings.remove(USER_SESSION_KEY)
        }
    }

    fun loadSession(): UserSession? {
        val sessionJson = settings.getStringOrNull(USER_SESSION_KEY)
        return if (sessionJson != null) {
            try {
                Json.decodeFromString<UserSession>(sessionJson)
            } catch (_: SerializationException) {
                clearSession()
                null
            }
        } else {
            null
        }
    }

    fun clearSession() {
        settings.remove(USER_SESSION_KEY)
        _userSession.value = null
    }

    companion object {
        private const val USER_SESSION_KEY = "user_session"
    }
}
