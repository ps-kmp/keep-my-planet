@file:Suppress("ktlint:standard:filename")

package pt.isel.keepmyplanet

import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application

fun main() =
    application {
        Window(
            onCloseRequest = ::exitApplication,
            title = "KeepMyPlanet",
        ) {
            App()
        }
    }
