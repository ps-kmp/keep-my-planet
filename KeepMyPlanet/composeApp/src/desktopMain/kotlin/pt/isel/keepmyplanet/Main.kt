package pt.isel.keepmyplanet

import androidx.compose.runtime.remember
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import dev.sargunv.maplibrecompose.compose.KcefProvider
import dev.sargunv.maplibrecompose.compose.MaplibreContextProvider
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
            KcefProvider {
                MaplibreContextProvider {
                    App(appViewModel)
                }
            }
        }
    }
