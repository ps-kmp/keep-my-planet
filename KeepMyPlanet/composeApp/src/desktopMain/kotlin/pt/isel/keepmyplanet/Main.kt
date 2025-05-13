package pt.isel.keepmyplanet

import androidx.compose.runtime.remember
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import pt.isel.keepmyplanet.data.api.createHttpClient
import pt.isel.keepmyplanet.data.service.AuthService
import pt.isel.keepmyplanet.data.service.ChatService
import pt.isel.keepmyplanet.data.service.UserService

fun main() =
    application {
        val appViewModel =
            remember {
                val httpClient = createHttpClient { null }
                AppViewModel(
                    AuthService(httpClient),
                    ChatService(httpClient),
                    UserService(httpClient),
                )
            }
        Window(
            onCloseRequest = ::exitApplication,
            title = "KeepMyPlanet",
        ) {
            App(appViewModel)
        }
    }
