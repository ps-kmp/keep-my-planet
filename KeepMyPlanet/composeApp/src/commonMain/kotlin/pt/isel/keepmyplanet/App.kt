package pt.isel.keepmyplanet

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import pt.isel.keepmyplanet.data.api.createHttpClient
import pt.isel.keepmyplanet.data.service.AuthService
import pt.isel.keepmyplanet.data.service.ChatService

@Suppress("ktlint:standard:function-naming")
@Composable
fun App() {
    val appViewModel = remember { AppViewModel() }
    val userSession by appViewModel.userSession.collectAsState()
    val currentRoute by appViewModel.currentRoute.collectAsState()

    val httpClient = createHttpClient(userSession?.token)
    val chatService = ChatService(httpClient)
    val authService = AuthService(httpClient)

    AppContent(
        route = currentRoute,
        navigate = appViewModel::navigate,
        updateSession = appViewModel::updateSession,
        authService = authService,
        chatService = chatService,
    )
}
