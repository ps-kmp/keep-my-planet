package pt.isel.keepmyplanet.ui.components

import androidx.compose.runtime.Composable

interface LocationProvider {
    val isPermissionGranted: Boolean

    fun requestPermission()

    fun requestLocationUpdate()
}

@Composable
expect fun rememberLocationProvider(
    onLocationUpdated: (latitude: Double, longitude: Double) -> Unit,
    onLocationError: () -> Unit,
): LocationProvider

expect val shouldShowUserLocationMarker: Boolean
