package pt.isel.keepmyplanet

import android.app.Application
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import org.koin.compose.koinInject
import org.koin.core.context.startKoin
import pt.isel.keepmyplanet.di.appModule

class KeepMyPlanetApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        startKoin {
            modules(appModule)
        }
    }
}

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            val appViewModel: AppViewModel = koinInject()
            val navStack by appViewModel.navStack.collectAsState()
            BackHandler(enabled = navStack.size > 1) {
                appViewModel.navigateBack()
            }
            App()
        }
    }
}
