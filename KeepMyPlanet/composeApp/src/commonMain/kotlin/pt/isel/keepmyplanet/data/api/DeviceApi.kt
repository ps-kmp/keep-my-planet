package pt.isel.keepmyplanet.data.api

import io.ktor.client.HttpClient
import io.ktor.client.request.setBody
import io.ktor.client.request.url
import io.ktor.http.HttpMethod
import pt.isel.keepmyplanet.data.http.executeRequestUnit
import pt.isel.keepmyplanet.dto.notification.RegisterDeviceRequest

class DeviceApi(
    private val httpClient: HttpClient,
) {
    private object Endpoints {
        const val DEVICES_BASE = "devices"

        fun register() = "$DEVICES_BASE/register"
    }

    suspend fun registerDevice(
        token: String,
        platform: String,
    ): Result<Unit> =
        httpClient.executeRequestUnit {
            method = HttpMethod.Post
            url(Endpoints.register())
            setBody(RegisterDeviceRequest(token, platform))
        }
}
