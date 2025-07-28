package pt.isel.keepmyplanet.service

import com.google.auth.oauth2.GoogleCredentials
import com.google.firebase.FirebaseApp
import com.google.firebase.FirebaseOptions
import com.google.firebase.messaging.FirebaseMessaging
import com.google.firebase.messaging.FirebaseMessagingException
import com.google.firebase.messaging.MessagingErrorCode
import io.ktor.server.config.ApplicationConfig
import java.io.File
import java.io.FileInputStream
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.slf4j.LoggerFactory
import pt.isel.keepmyplanet.domain.common.Id
import pt.isel.keepmyplanet.dto.notification.FcmMessage
import pt.isel.keepmyplanet.repository.UserDeviceRepository

class NotificationService(
    private val userDeviceRepository: UserDeviceRepository,
    config: ApplicationConfig,
) {
    private val log = LoggerFactory.getLogger(NotificationService::class.java)

    private val serviceAccountPath =
        System.getenv("FCM_SERVICE_ACCOUNT_PATH") ?: "/etc/secrets/fcm_service_account.json"

    private val firebaseMessaging: FirebaseMessaging? = initializeFirebase()

    private fun initializeFirebase(): FirebaseMessaging? {
        return try {
            if (FirebaseApp.getApps().isNotEmpty()) {
                return FirebaseMessaging.getInstance()
            }

            val serviceAccountFile = File(serviceAccountPath)
            if (!serviceAccountFile.exists()) {
                log.warn(
                    "FCM service account file not found at '$serviceAccountPath'. " +
                        "Push notifications are disabled.",
                )
                return null
            }

            val serviceAccountStream = FileInputStream(serviceAccountFile)
            val credentials = GoogleCredentials.fromStream(serviceAccountStream)
            val options = FirebaseOptions.builder().setCredentials(credentials).build()
            FirebaseApp.initializeApp(options)
            log.info("Firebase Admin SDK initialized successfully.")
            FirebaseMessaging.getInstance()
        } catch (e: Exception) {
            log.error(
                "Failed to initialize Firebase Admin SDK. Push notifications will be disabled.",
                e,
            )
            null
        }
    }

    suspend fun registerDevice(
        userId: Id,
        token: String,
        platform: String,
    ) {
        userDeviceRepository.addDevice(userId, token, platform)
    }

    suspend fun sendNotificationToUser(
        userId: Id,
        data: Map<String, String>,
    ) {
        val tokens = userDeviceRepository.findTokensByUserId(userId)
        tokens.forEach { token -> sendNotification(FcmMessage(token = token, data = data)) }
    }

    suspend fun sendNotificationToTopic(
        topic: String,
        data: Map<String, String>,
    ) {
        sendNotification(FcmMessage(topic = topic, data = data))
    }

    private suspend fun sendNotification(message: FcmMessage) {
        val fbMessaging =
            firebaseMessaging ?: run {
                log.warn("Firebase not initialized, skipping notification.")
                return
            }

        val fcmMessageBuilder =
            com.google.firebase.messaging.Message
                .builder()
        message.data?.let { fcmMessageBuilder.putAllData(it) }

        if (message.token != null) {
            fcmMessageBuilder.setToken(message.token)
        } else if (message.topic != null) {
            fcmMessageBuilder.setTopic(message.topic)
        } else {
            return // Should not happen due to FcmMessage's init block
        }

        try {
            val response =
                withContext(Dispatchers.IO) {
                    fbMessaging.send(fcmMessageBuilder.build())
                }
            log.info("Successfully sent message: $response")
        } catch (e: FirebaseMessagingException) {
            log.error("Failed to send FCM message. Error code: ${e.messagingErrorCode}", e)
            if (message.token != null &&
                (
                    e.messagingErrorCode == MessagingErrorCode.UNREGISTERED ||
                        e.messagingErrorCode == MessagingErrorCode.INVALID_ARGUMENT
                )
            ) {
                log.warn(
                    "Token ${message.token} is invalid (${e.messagingErrorCode}). " +
                        "Removing from database.",
                )
                userDeviceRepository.removeDevice(message.token.toString())
            }
        } catch (e: Exception) {
            log.error("Exception sending FCM notification via Admin SDK", e)
        }
    }

    fun subscribeToTopic(
        tokens: List<String>,
        topic: String,
    ) {
        firebaseMessaging ?: return
        if (tokens.isEmpty()) return
        try {
            val response = firebaseMessaging.subscribeToTopic(tokens, topic)
            if (response.failureCount > 0) {
                log.warn(
                    "Failed to subscribe some tokens to topic '$topic'. " +
                        "Failures: ${response.failureCount}",
                )
                response.errors.forEach { log.error("Subscription error: ${it.reason}") }
            }
        } catch (e: FirebaseMessagingException) {
            log.error("Failed to subscribe to topic '$topic'", e)
        }
    }

    fun unsubscribeFromTopic(
        tokens: List<String>,
        topic: String,
    ) {
        firebaseMessaging ?: return
        if (tokens.isEmpty()) return
        try {
            firebaseMessaging.unsubscribeFromTopic(tokens, topic)
        } catch (e: FirebaseMessagingException) {
            log.error("Failed to unsubscribe from topic '$topic'", e)
        }
    }
}
