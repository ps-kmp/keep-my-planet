package pt.isel.keepmyplanet

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.tooling.preview.Preview
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.compose.viewModel
import io.ktor.client.engine.okhttp.OkHttp

class MainActivity : ComponentActivity() {
    @Suppress("UNCHECKED_CAST")
    private val factory =
        object : ViewModelProvider.Factory {
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                if (modelClass.isAssignableFrom(AppViewModel::class.java)) {
                    return AppViewModel(OkHttp) as T
                }
                throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            val appViewModel: AppViewModel = viewModel(factory = factory)
            val navStack by appViewModel.navStack.collectAsState()

            BackHandler(enabled = navStack.size > 1) {
                appViewModel.navigateBack()
            }

            App(appViewModel)
        }
    }
}

@Suppress("ktlint:standard:function-naming")
@Preview
@Composable
fun AppAndroidPreview() {
    val appViewModel = AppViewModel(OkHttp)
    App(appViewModel)
}
