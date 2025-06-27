package pt.isel.keepmyplanet.ui.components

import androidx.compose.runtime.Composable

@Composable
actual fun rememberGpsLocationProvider(
    onLocationUpdated: (latitude: Double, longitude: Double) -> Unit,
): GpsLocationProvider =
    object : GpsLocationProvider {
        // The browser handles permissions implicitly on each request.
        override val isPermissionGranted: Boolean = true

        override fun requestPermission() {
            // No-op, browser handles it.
        }

        override fun requestLocationUpdate() {
            TODO()
            /*
            if (window.navigator.geolocation == null) {
                console.error("Geolocation is not supported by this browser.")
                return
            }

            window.navigator.geolocation.getCurrentPosition(
                success = { position: GeolocationPosition ->
                    onLocationUpdated(position.coords.latitude, position.coords.longitude)
                },
                error = { error: GeolocationPositionError ->
                    console.error("Error getting location: Code ${error.code} - ${error.message}")
                },
            )
             */
        }
    }
