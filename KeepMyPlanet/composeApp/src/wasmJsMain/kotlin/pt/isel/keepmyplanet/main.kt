@file:Suppress("ktlint:standard:filename")

package pt.isel.keepmyplanet

import androidx.compose.runtime.remember
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.window.ComposeViewport
import io.ktor.client.engine.js.Js
import kotlinx.browser.document

@OptIn(ExperimentalComposeUiApi::class)
fun main() {
    ComposeViewport(document.body!!) {
        val appViewModel = remember { AppViewModel(Js) }
        App(appViewModel)
    }
}
