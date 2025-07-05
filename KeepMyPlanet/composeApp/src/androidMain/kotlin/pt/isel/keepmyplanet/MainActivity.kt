package pt.isel.keepmyplanet

import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberPermissionState
import com.google.android.gms.tasks.OnCompleteListener
import com.google.firebase.messaging.FirebaseMessaging
import org.koin.android.ext.android.inject
import org.koin.android.ext.koin.androidContext
import org.koin.core.context.startKoin
import pt.isel.keepmyplanet.data.service.CacheCleanupService
import pt.isel.keepmyplanet.di.appModule
import pt.isel.keepmyplanet.di.cacheModule

class KeepMyPlanetApplication : Application() {
    private val cacheCleanupService: CacheCleanupService by inject()

    override fun onCreate() {
        super.onCreate()
        startKoin {
            androidContext(this@KeepMyPlanetApplication)
            modules(appModule, cacheModule)
        }
        createNotificationChannel()
        cacheCleanupService.performCleanup()
    }

    private fun createNotificationChannel() {
        val name = "KeepMyPlanet Channel"
        val descriptionText = "Notifications for event updates and messages"
        val importance = NotificationManager.IMPORTANCE_DEFAULT
        val channel =
            NotificationChannel(FirebasePushNotificationService.CHANNEL_ID, name, importance)
                .apply { description = descriptionText }
        val notificationManager = getSystemService(NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.createNotificationChannel(channel)
    }
}

@OptIn(ExperimentalPermissionsApi::class)
class MainActivity : ComponentActivity() {
    companion object {
        const val EXTRA_EVENT_ID = "event_id"
    }

    private val appViewModel: AppViewModel by inject()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        handleIntent(intent)

        setContent {
            val uiState by appViewModel.uiState.collectAsState()

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                val notificationPermissionState =
                    rememberPermissionState(
                        android.Manifest.permission.POST_NOTIFICATIONS,
                    )
                if (!notificationPermissionState.status.isGranted) {
                    LaunchedEffect(Unit) {
                        notificationPermissionState.launchPermissionRequest()
                    }
                }
            }

            LaunchedEffect(uiState.userSession) {
                if (uiState.userSession != null) {
                    registerDeviceToken()
                }
            }

            BackHandler(enabled = uiState.navStack.size > 1) {
                appViewModel.navigateBack()
            }
            App()
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        handleIntent(intent)
    }

    private fun handleIntent(intent: Intent?) {
        intent?.getStringExtra(EXTRA_EVENT_ID)?.let { eventId ->
            Log.d("NAV_FROM_NOTIF", "Received eventId: $eventId, navigating...")
            appViewModel.handleNotificationNavigation(eventId)
        }
    }

    private fun registerDeviceToken() {
        FirebaseMessaging.getInstance().token.addOnCompleteListener(
            OnCompleteListener { task ->
                if (!task.isSuccessful) {
                    Log.w("FCM_REG", "Fetching FCM registration token failed", task.exception)
                    return@OnCompleteListener
                }

                val token = task.result
                Log.d("FCM_REG", "Token is $token. Registering with server.")

                appViewModel.registerDeviceToken(token)
            },
        )
    }
}
