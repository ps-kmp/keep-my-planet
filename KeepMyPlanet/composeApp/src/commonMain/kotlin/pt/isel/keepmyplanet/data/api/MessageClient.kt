package pt.isel.keepmyplanet.data.api

import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.plugins.sse.sse
import io.ktor.client.request.get
import io.ktor.client.request.headers
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.contentType
import io.ktor.http.isSuccess
import io.ktor.sse.ServerSentEvent
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.filterIsInstance
import kotlinx.coroutines.launch
import kotlinx.serialization.json.Json
import pt.isel.keepmyplanet.SERVER_PORT
import pt.isel.keepmyplanet.data.model.UserSession
import pt.isel.keepmyplanet.dto.message.AddMessageRequest
import pt.isel.keepmyplanet.dto.message.MessageResponse

class MessageClient(
    private val client: HttpClient,
    private val baseUrl: String = "http://localhost:${SERVER_PORT}",
) {
    suspend fun sendMessage(
        eventId: UInt,
        message: AddMessageRequest,
    ): Result<MessageResponse> =
        runCatching {
            val response =
                client.post("$baseUrl/event/$eventId/chat") {
                    contentType(ContentType.Application.Json)
                    setBody(message)
                    headers {
                        // append("User-Id", userId.toString())
                        append("Accept", "application/json")
                    }
                }

            if (response.status.isSuccess()) {
                response.body()
            } else {
                throw Exception("Failed to send message: ${response.status}")
            }
        }

    suspend fun getEventMessages(eventId: UInt): Result<List<MessageResponse>> =
        runCatching {
            val response = client.get("$baseUrl/event/$eventId/chat")

            if (response.status.isSuccess()) {
                response.body()
            } else {
                throw Exception("Failed to retrieve messages: ${response.status}")
            }
        }

    // Simular entrada num evento (nao sera assim quando entidade Evento estiver implementada no servidor)
    suspend fun joinEvent(
        username: String,
        eventName: String,
    ): Result<UserSession> {
        // comunicar com servidor para autenticar o user
        // e obter o eventId correto
        return Result.success(
            UserSession(
                username = username,
                eventId = 1u, // hardcoded
            ),
        )
    }

    fun startSse(
        eventId: UInt,
        scope: CoroutineScope,
        onMessage: (MessageResponse) -> Unit,
    ): Job =
        scope.launch {
            client.sse("$baseUrl/event/$eventId/chat/stream") {
                incoming
                    .filterIsInstance<ServerSentEvent>()
                    .collect { event ->
                        event.data?.let { json ->
                            runCatching {
                                Json.decodeFromString<MessageResponse>(json)
                            }.onSuccess { msg ->
                                onMessage(msg)
                            }.onFailure {
                                println("Failed to parse message: ${it.message}")
                            }
                        }
                    }
            }
        }
}
