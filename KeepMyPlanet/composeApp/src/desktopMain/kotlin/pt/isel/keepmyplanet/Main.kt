package pt.isel.keepmyplanet

import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import org.koin.core.context.GlobalContext.get
import pt.isel.keepmyplanet.data.service.CacheCleanupService
import pt.isel.keepmyplanet.di.initKoin

fun main() {
    initKoin()
    val koin = get()
    koin.get<CacheCleanupService>().performCleanup()

    application {
        Window(
            onCloseRequest = ::exitApplication,
            title = "KeepMyPlanet",
        ) {
            App()
        }
    }
}
