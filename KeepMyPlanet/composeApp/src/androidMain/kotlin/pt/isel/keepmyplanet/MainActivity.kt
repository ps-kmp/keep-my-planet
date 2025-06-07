package pt.isel.keepmyplanet

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.tooling.preview.Preview
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.compose.viewModel
import io.ktor.client.engine.okhttp.OkHttp
import pt.isel.keepmyplanet.di.AppContainer

class MainActivity : ComponentActivity() {
    private val appContainer by lazy { AppContainer(OkHttp) }

    @Suppress("UNCHECKED_CAST")
    private val viewModelFactory =
        object : ViewModelProvider.Factory {
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                if (modelClass.isAssignableFrom(AppViewModel::class.java)) {
                    return AppViewModel(appContainer) as T
                }
                throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            val appViewModel: AppViewModel = viewModel(factory = viewModelFactory)
            App(appViewModel)
        }
    }
}

@Preview
@Composable
fun AppAndroidPreview() {
    val container = remember { AppContainer(OkHttp) }
    val appViewModel = remember { AppViewModel(container) }
    App(appViewModel)
}
