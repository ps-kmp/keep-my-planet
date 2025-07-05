package pt.isel.keepmyplanet

import android.Manifest
import android.app.PendingIntent
import android.content.Intent
import android.content.pm.PackageManager
import android.util.Log
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel

class FirebasePushNotificationService : FirebaseMessagingService() {
    private val serviceScope = CoroutineScope(Dispatchers.IO + SupervisorJob())

    override fun onNewToken(token: String) {
        super.onNewToken(token)
        Log.d("FCM", "New token: $token")
    }

    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        super.onMessageReceived(remoteMessage)

        if (remoteMessage.data.isNotEmpty()) {
            val data = remoteMessage.data
            Log.d("FCM", "Message Data payload: $data")

            val title = data["title"] ?: "New Notification"
            val body = data["body"] ?: ""
            val eventId = data["eventId"]

            sendNotification(title, body, eventId)
        } else {
            remoteMessage.notification?.let {
                Log.d("FCM", "Message Notification Body: ${it.body}")
                sendNotification(it.title ?: "New Notification", it.body ?: "", null)
            }
        }
    }

    private fun sendNotification(
        title: String,
        messageBody: String,
        eventId: String?,
    ) {
        val intent =
            Intent(this, MainActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                putExtra(MainActivity.EXTRA_EVENT_ID, eventId)
            }

        val pendingIntent: PendingIntent =
            PendingIntent.getActivity(
                this,
                eventId?.hashCode() ?: 0,
                intent,
                PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT,
            )

        val notificationManager = NotificationManagerCompat.from(this)

        val notification =
            NotificationCompat
                .Builder(this, CHANNEL_ID)
                .setSmallIcon(R.mipmap.ic_launcher)
                .setContentTitle(title)
                .setContentText(messageBody)
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .setContentIntent(pendingIntent)
                .setAutoCancel(true)
                .build()

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) ==
            PackageManager.PERMISSION_GRANTED
        ) {
            val notificationId = eventId?.hashCode() ?: GENERAL_NOTIFICATION_ID
            notificationManager.notify(notificationId, notification)
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        serviceScope.cancel()
    }

    companion object {
        const val CHANNEL_ID = "keepmyplanet_notifications"
        private const val GENERAL_NOTIFICATION_ID = 1
    }
}
