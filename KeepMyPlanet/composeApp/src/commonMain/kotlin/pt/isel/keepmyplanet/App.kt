package pt.isel.keepmyplanet

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import pt.isel.keepmyplanet.data.api.createHttpClient
import pt.isel.keepmyplanet.data.model.UserSession
import pt.isel.keepmyplanet.data.service.AuthService
import pt.isel.keepmyplanet.data.service.ChatService
import pt.isel.keepmyplanet.navigation.AppRoute

@Suppress("ktlint:standard:function-naming")
@Composable
fun App() {
    var userSession by remember { mutableStateOf<UserSession?>(null) }
    var currentRoute by remember { mutableStateOf<AppRoute>(AppRoute.Login) }

    val httpClient = remember(userSession?.token) { createHttpClient(userSession?.token) }
    val chatService = remember(httpClient) { ChatService(httpClient) }
    val authService = remember(httpClient) { AuthService(httpClient) }

    val navigate: (AppRoute) -> Unit = { newRoute -> currentRoute = newRoute }
    val updateSession: (UserSession?) -> Unit = { session -> userSession = session }

    AppContent(
        route = currentRoute,
        navigate = navigate,
        updateSession = updateSession,
        authService = authService,
        chatService = chatService,
    )
}
