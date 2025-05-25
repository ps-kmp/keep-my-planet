@file:Suppress("ktlint:standard:filename")

package pt.isel.keepmyplanet

import androidx.compose.runtime.remember
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.window.ComposeViewport
import kotlinx.browser.document
import pt.isel.keepmyplanet.data.api.createHttpClient

@OptIn(ExperimentalComposeUiApi::class)
fun main() {
    ComposeViewport(document.body!!) {
        lateinit var appViewModel: AppViewModel
        val httpClient = createHttpClient { appViewModel.userSession.value?.token }
        appViewModel = remember { AppViewModel(httpClient) }
        App(appViewModel)
    }
}
