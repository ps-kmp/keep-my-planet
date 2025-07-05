package pt.isel.keepmyplanet.ui.components

import android.Manifest
import android.annotation.SuppressLint
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.platform.LocalContext
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberPermissionState
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.google.android.gms.tasks.CancellationTokenSource

@OptIn(ExperimentalPermissionsApi::class)
@SuppressLint("MissingPermission")
@Composable
actual fun rememberLocationProvider(
    onLocationUpdated: (latitude: Double, longitude: Double) -> Unit,
): LocationProvider {
    val context = LocalContext.current
    val locationClient = remember { LocationServices.getFusedLocationProviderClient(context) }
    val onLocationUpdatedState by rememberUpdatedState(onLocationUpdated)
    val cancellationTokenSource = remember { CancellationTokenSource() }

    DisposableEffect(Unit) {
        onDispose {
            cancellationTokenSource.cancel()
        }
    }

    val permissionState =
        rememberPermissionState(Manifest.permission.ACCESS_FINE_LOCATION) { isGranted ->
            if (isGranted) {
                locationClient
                    .getCurrentLocation(
                        Priority.PRIORITY_HIGH_ACCURACY,
                        cancellationTokenSource.token,
                    ).addOnSuccessListener { location ->
                        location?.let { onLocationUpdatedState(it.latitude, it.longitude) }
                    }
            }
        }

    return remember(permissionState, locationClient, onLocationUpdatedState) {
        object : LocationProvider {
            override val isPermissionGranted: Boolean
                get() = permissionState.status.isGranted

            override fun requestPermission() {
                permissionState.launchPermissionRequest()
            }

            override fun requestLocationUpdate() {
                if (permissionState.status.isGranted) {
                    locationClient
                        .getCurrentLocation(
                            Priority.PRIORITY_HIGH_ACCURACY,
                            cancellationTokenSource.token,
                        ).addOnSuccessListener { location ->
                            location?.let { onLocationUpdatedState(it.latitude, it.longitude) }
                        }
                }
            }
        }
    }
}

actual val shouldShowUserLocationMarker: Boolean = true
