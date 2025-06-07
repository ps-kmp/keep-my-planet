package pt.isel.keepmyplanet

import androidx.compose.runtime.remember
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import io.ktor.client.engine.cio.CIO
import pt.isel.keepmyplanet.di.AppContainer

fun main() =
    application {
        val container = remember { AppContainer(CIO) }
        val appViewModel = remember { AppViewModel(container) }

        Window(
            onCloseRequest = ::exitApplication,
            title = "KeepMyPlanet",
        ) {
            App(appViewModel)
        }
    }
