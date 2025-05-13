@file:Suppress("ktlint:standard:filename")

package pt.isel.keepmyplanet

import androidx.compose.runtime.remember
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.window.ComposeViewport
import kotlinx.browser.document
import pt.isel.keepmyplanet.data.api.createHttpClient
import pt.isel.keepmyplanet.data.service.AuthService
import pt.isel.keepmyplanet.data.service.ChatService
import pt.isel.keepmyplanet.data.service.UserService

@OptIn(ExperimentalComposeUiApi::class)
fun main() {
    ComposeViewport(document.body!!) {
        val appViewModel =
            remember {
                val httpClient = createHttpClient { null }
                AppViewModel(
                    AuthService(httpClient),
                    ChatService(httpClient),
                    UserService(httpClient),
                )
            }
        App(appViewModel)
    }
}
