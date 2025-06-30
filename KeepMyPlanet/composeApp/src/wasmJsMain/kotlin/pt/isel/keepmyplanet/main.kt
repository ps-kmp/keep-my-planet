@file:Suppress("ktlint:standard:filename")

package pt.isel.keepmyplanet

import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.window.ComposeViewport
import kotlinx.browser.document
import org.koin.core.context.GlobalContext.get
import pt.isel.keepmyplanet.data.service.CacheCleanupService
import pt.isel.keepmyplanet.di.initKoin

@OptIn(ExperimentalComposeUiApi::class)
fun main() {
    initKoin()
    val koin = get()
    koin.get<CacheCleanupService>().performCleanup()

    ComposeViewport(document.body!!) {
        App()
    }
}
