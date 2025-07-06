package pt.isel.keepmyplanet.service

import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get
import pt.isel.keepmyplanet.dto.geocoding.IpLocationResponse

class IpGeocodingService(
    private val httpClient: HttpClient,
) {
    suspend fun getIpBasedLocation(): Result<IpLocationResponse> =
        runCatching {
            httpClient.get("https://ipinfo.io/json").body()
        }
}
