package pt.isel.keepmyplanet.data.service

import io.ktor.client.HttpClient
import io.ktor.client.plugins.sse.sse
import io.ktor.client.request.setBody
import io.ktor.client.request.url
import io.ktor.http.HttpMethod
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.serialization.json.Json
import pt.isel.keepmyplanet.data.api.executeRequest
import pt.isel.keepmyplanet.data.api.executeRequestUnit
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
        httpClient.executeRequest {
            method = HttpMethod.Get
            url(Endpoints.messages(eventId))
        }

    suspend fun sendMessage(
        eventId: UInt,
        content: String,
    ): Result<Unit> =
        httpClient.executeRequestUnit {
            method = HttpMethod.Post
            url(Endpoints.messages(eventId))
            setBody(CreateMessageRequest(content))
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
