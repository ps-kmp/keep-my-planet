package pt.isel.keepmyplanet

import androidx.compose.runtime.remember
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import io.ktor.client.engine.java.Java
import pt.isel.keepmyplanet.di.AppContainer

fun main() =
    application {
        val container = remember { AppContainer(Java) }

        Window(
            onCloseRequest = ::exitApplication,
            title = "KeepMyPlanet",
        ) {
            App(container)
        }
    }
