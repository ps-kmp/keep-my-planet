package pt.isel.keepmyplanet.ui.components

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.rememberUpdatedState
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.request.get
import io.ktor.serialization.kotlinx.json.json
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json

@Serializable
private data class IpLocationResponse(
    val status: String,
    val lat: Double?,
    val lon: Double?,
)

@Composable
actual fun rememberLocationProvider(
    onLocationUpdated: (latitude: Double, longitude: Double) -> Unit,
): LocationProvider {
    val coroutineScope = rememberCoroutineScope()
    val onLocationUpdatedState by rememberUpdatedState(onLocationUpdated)
    val httpClient =
        remember {
            HttpClient {
                install(ContentNegotiation) {
                    json(Json { ignoreUnknownKeys = true })
                }
            }
        }

    return remember(httpClient) {
        object : LocationProvider {
            override val isPermissionGranted: Boolean = true

            override fun requestPermission() {}

            override fun requestLocationUpdate() {
                coroutineScope.launch(Dispatchers.IO) {
                    try {
                        val response: IpLocationResponse =
                            httpClient.get("https://ip-api.com/json").body()
                        if (response.status == "success" &&
                            response.lat != null &&
                            response.lon != null
                        ) {
                            onLocationUpdatedState(response.lat, response.lon)
                        }
                    } catch (e: Exception) {
                        println("Could not fetch IP-based location: ${e.message}")
                    }
                }
            }
        }
    }
}

actual val shouldShowUserLocationMarker: Boolean = false
