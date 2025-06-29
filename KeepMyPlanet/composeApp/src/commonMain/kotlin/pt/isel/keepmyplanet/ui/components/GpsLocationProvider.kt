package pt.isel.keepmyplanet.ui.components

import androidx.compose.runtime.Composable

interface GpsLocationProvider {
    val isPermissionGranted: Boolean

    fun requestPermission()

    fun requestLocationUpdate()
}

@Composable
expect fun rememberGpsLocationProvider(
    onLocationUpdated: (latitude: Double, longitude: Double) -> Unit,
): GpsLocationProvider

expect val shouldShowUserLocationMarker: Boolean
