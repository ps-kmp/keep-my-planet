package pt.isel.keepmyplanet

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import pt.isel.keepmyplanet.di.AppContainer

class MainActivity : ComponentActivity() {
    private val container by lazy { AppContainer() }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            val navStack by container.appViewModel.navStack.collectAsState()

            BackHandler(enabled = navStack.size > 1) {
                container.appViewModel.navigateBack()
            }

            App(container)
        }
    }
}
