package pt.isel.keepmyplanet.service

import com.google.auth.oauth2.GoogleCredentials
import com.google.firebase.FirebaseApp
import com.google.firebase.FirebaseOptions
import com.google.firebase.messaging.FirebaseMessaging
import com.google.firebase.messaging.FirebaseMessagingException
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.request.bearerAuth
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.contentType
import io.ktor.http.isSuccess
import io.ktor.serialization.kotlinx.json.json
import io.ktor.server.config.ApplicationConfig
import java.io.FileInputStream
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json
import org.slf4j.LoggerFactory
import pt.isel.keepmyplanet.domain.common.Id
import pt.isel.keepmyplanet.dto.notification.FcmErrorResponse
import pt.isel.keepmyplanet.dto.notification.FcmMessage
import pt.isel.keepmyplanet.dto.notification.FcmRequest
import pt.isel.keepmyplanet.repository.UserDeviceRepository

class NotificationService(
    private val userDeviceRepository: UserDeviceRepository,
    config: ApplicationConfig,
) {
    private val httpClient =
        HttpClient {
            install(ContentNegotiation) {
                json(
                    Json {
                        prettyPrint = true
                        isLenient = true
                        ignoreUnknownKeys = true
                    },
                )
            }
        }
    private val fcmProjectId = config.property("fcm.projectId").getString()
    private val fcmUrl = "https://fcm.googleapis.com/v1/projects/$fcmProjectId/messages:send"
    private val log = LoggerFactory.getLogger(NotificationService::class.java)

    private val serviceAccountPath =
        System.getenv("FCM_SERVICE_ACCOUNT_PATH") ?: "/etc/secrets/fcm_service_account.json"

    private val firebaseMessaging: FirebaseMessaging by lazy {
        if (FirebaseApp.getApps().isEmpty()) {
            val serviceAccountStream = FileInputStream(serviceAccountPath)
            val credentials = GoogleCredentials.fromStream(serviceAccountStream)
            val options =
                FirebaseOptions
                    .builder()
                    .setCredentials(credentials)
                    .build()
            FirebaseApp.initializeApp(options)
        }
        FirebaseMessaging.getInstance()
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
        try {
            val accessToken = getAccessToken()
            val request = FcmRequest(message = message)
            val response =
                httpClient.post(fcmUrl) {
                    bearerAuth(accessToken)
                    contentType(ContentType.Application.Json)
                    setBody(request)
                }

            if (!response.status.isSuccess()) {
                handleFcmError(response.body(), message.token)
            }
        } catch (e: Exception) {
            log.error("Exception sending FCM notification", e)
        }
    }

    private suspend fun handleFcmError(
        errorBody: String,
        token: String?,
    ) {
        log.error("Failed to send notification: $errorBody")
        if (token == null) return

        try {
            val fcmError = Json.decodeFromString<FcmErrorResponse>(errorBody)
            val errorCode =
                fcmError.error.details
                    ?.firstOrNull()
                    ?.errorCode
            if (errorCode == "UNREGISTERED" || fcmError.error.status == "INVALID_ARGUMENT") {
                log.warn(
                    "Token $token is invalid (reason: ${errorCode ?: fcmError.error.status}). " +
                        "Removing from database.",
                )
                userDeviceRepository.removeDevice(token)
            }
        } catch (e: Exception) {
            log.error("Failed to parse FCM error response: $errorBody", e)
        }
    }

    fun subscribeToTopic(
        tokens: List<String>,
        topic: String,
    ) {
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
        if (tokens.isEmpty()) return
        try {
            firebaseMessaging.unsubscribeFromTopic(tokens, topic)
        } catch (e: FirebaseMessagingException) {
            log.error("Failed to unsubscribe from topic '$topic'", e)
        }
    }

    private suspend fun getAccessToken(): String =
        withContext(Dispatchers.IO) {
            val serviceAccountStream = FileInputStream(serviceAccountPath)
            val credentials =
                GoogleCredentials
                    .fromStream(serviceAccountStream)
                    .createScoped(listOf("https://www.googleapis.com/auth/firebase.messaging"))
            credentials.refreshIfExpired()
            credentials.accessToken.tokenValue
        }
}
