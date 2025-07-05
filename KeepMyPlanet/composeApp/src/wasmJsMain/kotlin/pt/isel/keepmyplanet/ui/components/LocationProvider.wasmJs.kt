package pt.isel.keepmyplanet.ui.components

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState

@Composable
actual fun rememberLocationProvider(
    onLocationUpdated: (latitude: Double, longitude: Double) -> Unit,
): LocationProvider {
    val onLocationUpdatedState by rememberUpdatedState(onLocationUpdated)

    return remember {
        object : LocationProvider {
            override val isPermissionGranted: Boolean = true

            override fun requestPermission() {}

            override fun requestLocationUpdate() {
                jsRequestLocation(
                    onSuccess = { lat, lon -> onLocationUpdatedState(lat, lon) },
                    onError = { _, _ -> },
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
                rnError(-1, 'Geolocation is not supported by this browser.');
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
