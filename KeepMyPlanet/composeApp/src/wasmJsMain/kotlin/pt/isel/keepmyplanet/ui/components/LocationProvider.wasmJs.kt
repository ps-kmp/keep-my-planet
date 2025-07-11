package pt.isel.keepmyplanet.ui.components

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.rememberUpdatedState
import kotlinx.coroutines.launch
import org.koin.compose.koinInject
import pt.isel.keepmyplanet.data.repository.GeocodingApiRepository

@Composable
actual fun rememberLocationProvider(
    onLocationUpdated: (latitude: Double, longitude: Double) -> Unit,
    onLocationError: () -> Unit,
): LocationProvider {
    val coroutineScope = rememberCoroutineScope()
    val onLocationUpdatedState by rememberUpdatedState(onLocationUpdated)
    val onLocationErrorState by rememberUpdatedState(onLocationError)
    val geocodingRepository: GeocodingApiRepository = koinInject()

    fun requestIpLocationFallback() {
        coroutineScope.launch {
            fetchIpBasedLocation(
                geocodingRepository = geocodingRepository,
                onSuccess = { lat, lon -> onLocationUpdatedState(lat, lon) },
                onError = { onLocationErrorState() },
            )
        }
    }

    return remember {
        object : LocationProvider {
            override val isPermissionGranted: Boolean = true

            override fun requestPermission() {
                // Not applicable for wasmJs in this context
            }

            override fun requestLocationUpdate() {
                jsRequestLocation(
                    onSuccess = { lat, lon -> onLocationUpdatedState(lat, lon) },
                    onError = { _, _ -> requestIpLocationFallback() },
                )
            }
        }
    }
}

actual val shouldShowUserLocationMarker: Boolean = false

private fun jsRequestLocation(
    onSuccess: (latitude: Double, longitude: Double) -> Unit,
    onError: (code: Int, message: String) -> Unit,
): Unit =
    js(
        """{
            if (!('geolocation' in navigator)) {
                console.error('Geolocation is not supported by this browser.');
                onError(-1, 'Geolocation is not supported by this browser.');
                return;
            }

            navigator.geolocation.getCurrentPosition(
                (position) => {
                    onSuccess(position.coords.latitude, position.coords.longitude);
                },
                (error) => {
                    console.error(`Error getting location`);
                    onError(error.code, error.message);
                }
            );
        }""",
    )
