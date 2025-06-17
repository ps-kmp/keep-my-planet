package pt.isel.keepmyplanet

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import io.ktor.client.engine.android.Android
import org.jetbrains.compose.ui.tooling.preview.Preview
import pt.isel.keepmyplanet.di.AppContainer

class MainActivity : ComponentActivity() {
    private val container by lazy { AppContainer(Android) }

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

@Suppress("ktlint:standard:function-naming")
@Preview
@Composable
fun AppAndroidPreview() {
    val appViewModel = AppContainer(Android)
    App(appViewModel)
}
