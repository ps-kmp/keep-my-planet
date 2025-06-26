package pt.isel.keepmyplanet

import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import pt.isel.keepmyplanet.di.initKoin

fun main() {
    initKoin()
    application {
        Window(
            onCloseRequest = ::exitApplication,
            title = "KeepMyPlanet",
        ) {
            App()
        }
    }
}
