package pt.isel.keepmyplanet

import androidx.compose.runtime.remember
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import pt.isel.keepmyplanet.data.api.createHttpClient

fun main() =
    application {
        lateinit var appViewModel: AppViewModel
        val httpClient = createHttpClient { appViewModel.userSession.value?.token }
        appViewModel = remember { AppViewModel(httpClient) }

        Window(
            onCloseRequest = ::exitApplication,
            title = "KeepMyPlanet",
        ) {
            App(appViewModel)
        }
    }
