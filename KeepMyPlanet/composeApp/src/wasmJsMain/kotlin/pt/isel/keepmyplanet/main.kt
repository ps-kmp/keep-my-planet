@file:Suppress("ktlint:standard:filename")

package pt.isel.keepmyplanet

import androidx.compose.runtime.remember
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.window.ComposeViewport
import io.ktor.client.engine.js.Js
import kotlinx.browser.document
import pt.isel.keepmyplanet.di.AppContainer

@OptIn(ExperimentalComposeUiApi::class)
fun main() {
    ComposeViewport(document.body!!) {
        val container = remember { AppContainer(Js) }
        App(container)
    }
}
