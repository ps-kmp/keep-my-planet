package pt.isel.keepmyplanet.data.service

import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.plugins.sse.sse
import io.ktor.client.request.get
import io.ktor.client.request.header
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.contentType
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.serialization.json.Json
import pt.isel.keepmyplanet.dto.message.CreateMessageRequest
import pt.isel.keepmyplanet.dto.message.MessageResponse

class ChatService(
    private val httpClient: HttpClient,
) {
    private object Endpoints {
        fun messages(eventId: UInt) = "events/$eventId/chat"

        fun messagesSse(eventId: UInt) = "events/$eventId/chat/stream"
    }

    suspend fun getMessages(eventId: UInt): Result<List<MessageResponse>> =
        runCatching {
            httpClient.get(Endpoints.messages(eventId)).body<List<MessageResponse>>()
        }

    suspend fun sendMessage(
        eventId: UInt,
        userId: UInt,
        content: String,
    ): Result<Unit> =
        runCatching {
            httpClient
                .post(Endpoints.messages(eventId)) {
                    contentType(ContentType.Application.Json)
                    setBody(CreateMessageRequest(content))
                    header("X-Mock-User-Id", userId.toString())
                }
            Unit
        }

    fun listenToMessages(eventId: UInt): Flow<Result<MessageResponse>> =
        flow {
            try {
                httpClient.sse(Endpoints.messagesSse(eventId)) {
                    incoming.collect { event ->
                        val jsonString = event.data
                        if (!jsonString.isNullOrBlank()) {
                            val parseResult =
                                runCatching {
                                    Json.decodeFromString<MessageResponse>(jsonString)
                                }
                            emit(parseResult)
                        }
                    }
                }
            } catch (e: Exception) {
                emit(Result.failure(e))
            }
        }
}
