package pt.isel.keepmyplanet.ui.components

import androidx.compose.runtime.Composable

@Composable
actual fun rememberGpsLocationProvider(
    onLocationUpdated: (latitude: Double, longitude: Double) -> Unit,
): GpsLocationProvider {
    // Location services are not available on desktop.
    return object : GpsLocationProvider {
        override val isPermissionGranted: Boolean = false

        override fun requestPermission() {
            // No-op
        }

        override fun requestLocationUpdate() {
            // No-op
        }
    }
}
