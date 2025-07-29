package pt.isel.keepmyplanet.service

import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get
import io.ktor.server.config.ApplicationConfig
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import pt.isel.keepmyplanet.dto.geocoding.IpLocationResponse
import pt.isel.keepmyplanet.exception.InternalServerException
import pt.isel.keepmyplanet.exception.NotFoundException

class IpGeocodingService(
    private val httpClient: HttpClient,
    config: ApplicationConfig,
) {
    private val ipInfoToken = config.propertyOrNull("ipinfo.token")?.getString()

    suspend fun getIpBasedLocation(clientIp: String): Result<IpLocationResponse> =
        runCatching {
            if (ipInfoToken.isNullOrBlank()) {
                throw InternalServerException("IPINFO_TOKEN is not configured on the server.")
            }

            val url =
                if (clientIp == "127.0.0.1" || clientIp == "0:0:0:0:0:0:0:1") {
                    "https://ipinfo.io/json?token=$ipInfoToken"
                } else {
                    "https://ipinfo.io/$clientIp/json?token=$ipInfoToken"
                }

            val responseString: String = httpClient.get(url).body()
            val jsonElement = Json.parseToJsonElement(responseString)
            val loc = jsonElement.jsonObject["loc"]?.jsonPrimitive?.content

            if (loc == null) {
                throw NotFoundException("Could not determine location from IP address: $clientIp")
            }

            IpLocationResponse(loc = loc)
        }
}
