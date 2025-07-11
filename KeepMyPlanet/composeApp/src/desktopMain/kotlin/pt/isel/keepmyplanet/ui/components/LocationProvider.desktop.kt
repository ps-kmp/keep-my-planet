package pt.isel.keepmyplanet.ui.components

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState

@Composable
actual fun rememberLocationProvider(
    onLocationUpdated: (latitude: Double, longitude: Double) -> Unit,
    onLocationError: () -> Unit,
): LocationProvider {
    val onLocationErrorState by rememberUpdatedState(onLocationError)

    return remember {
        object : LocationProvider {
            override val isPermissionGranted: Boolean = true

            override fun requestPermission() {}

            override fun requestLocationUpdate() {
                onLocationErrorState()
            }
        }
    }
}

actual val shouldShowUserLocationMarker: Boolean = false
