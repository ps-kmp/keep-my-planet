@file:Suppress("ktlint:standard:no-wildcard-imports")

package pt.isel.keepmyplanet.data.api

import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.request.*
import io.ktor.http.*
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
        try {
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
                Result.success(response.body())
            } else {
                Result.failure(Exception("Erro ao enviar mensagem: ${response.status}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }

    suspend fun getEventMessages(eventId: UInt): Result<List<MessageResponse>> =
        try {
            val response = client.get("$baseUrl/event/$eventId/chat")

            if (response.status.isSuccess()) {
                Result.success(response.body())
            } else {
                Result.failure(Exception("Erro ao obter mensagens: ${response.status}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
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
}
