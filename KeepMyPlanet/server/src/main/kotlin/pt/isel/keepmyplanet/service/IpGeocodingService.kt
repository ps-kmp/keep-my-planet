package pt.isel.keepmyplanet.service

import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import pt.isel.keepmyplanet.dto.geocoding.IpLocationResponse
import pt.isel.keepmyplanet.exception.NotFoundException

class IpGeocodingService(
    private val httpClient: HttpClient,
) {
    suspend fun getIpBasedLocation(): Result<IpLocationResponse> =
        runCatching {
            val responseString: String = httpClient.get("https://ipinfo.io/json").body()
            val jsonElement = Json.parseToJsonElement(responseString)
            val loc = jsonElement.jsonObject["loc"]?.jsonPrimitive?.content

            if (loc == null) {
                throw NotFoundException("Could not determine location from IP address.")
            }

            IpLocationResponse(loc = loc)
        }
}
