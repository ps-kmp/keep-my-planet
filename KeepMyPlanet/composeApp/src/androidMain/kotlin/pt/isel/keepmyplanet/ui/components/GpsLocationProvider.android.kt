package pt.isel.keepmyplanet.ui.components

import android.Manifest
import android.annotation.SuppressLint
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
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
actual fun rememberGpsLocationProvider(
    onLocationUpdated: (latitude: Double, longitude: Double) -> Unit,
): GpsLocationProvider {
    val context = LocalContext.current
    val permissionState = rememberPermissionState(Manifest.permission.ACCESS_FINE_LOCATION)
    val locationClient = remember { LocationServices.getFusedLocationProviderClient(context) }

    return remember(permissionState, locationClient) {
        object : GpsLocationProvider {
            override val isPermissionGranted: Boolean
                get() = permissionState.status.isGranted

            override fun requestPermission() {
                permissionState.launchPermissionRequest()
            }

            override fun requestLocationUpdate() {
                if (isPermissionGranted) {
                    locationClient
                        .getCurrentLocation(
                            Priority.PRIORITY_HIGH_ACCURACY,
                            CancellationTokenSource().token,
                        ).addOnSuccessListener { location ->
                            location?.let { onLocationUpdated(it.latitude, it.longitude) }
                        }
                }
            }
        }
    }
}
