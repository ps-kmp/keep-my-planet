package pt.isel.keepmyplanet

import androidx.compose.runtime.remember
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import io.ktor.client.engine.cio.CIO

fun main() =
    application {
        val appViewModel = remember { AppViewModel(CIO) }

        Window(
            onCloseRequest = ::exitApplication,
            title = "KeepMyPlanet",
        ) {
            App(appViewModel)
        }
    }
