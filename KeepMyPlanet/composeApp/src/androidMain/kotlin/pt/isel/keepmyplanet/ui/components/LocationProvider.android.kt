package pt.isel.keepmyplanet.ui.components

import android.Manifest
import android.annotation.SuppressLint
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.platform.LocalContext
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberPermissionState
import com.google.android.gms.location.LocationServices

@OptIn(ExperimentalPermissionsApi::class)
@SuppressLint("MissingPermission")
@Composable
actual fun rememberLocationProvider(
    onLocationUpdated: (latitude: Double, longitude: Double) -> Unit,
    onLocationError: () -> Unit,
): LocationProvider {
    val context = LocalContext.current
    val locationClient = remember { LocationServices.getFusedLocationProviderClient(context) }
    val onLocationUpdatedState by rememberUpdatedState(onLocationUpdated)
    val onLocationErrorState by rememberUpdatedState(onLocationError)

    val permissionState = rememberPermissionState(Manifest.permission.ACCESS_FINE_LOCATION)

    return remember(permissionState) {
        object : LocationProvider {
            override val isPermissionGranted: Boolean
                get() = permissionState.status.isGranted

            override fun requestPermission() {
                if (!permissionState.status.isGranted) {
                    permissionState.launchPermissionRequest()
                }
            }

            override fun requestLocationUpdate() {
                if (permissionState.status.isGranted) {
                    locationClient.lastLocation
                        .addOnSuccessListener { location ->
                            location?.let { onLocationUpdatedState(it.latitude, it.longitude) }
                                ?: onLocationErrorState()
                        }.addOnFailureListener {
                            onLocationErrorState()
                        }
                } else {
                    onLocationErrorState()
                }
            }
        }
    }
}

actual val shouldShowUserLocationMarker: Boolean = true
