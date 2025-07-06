package pt.isel.keepmyplanet.ui.components

import android.Manifest
import android.annotation.SuppressLint
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.platform.LocalContext
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberPermissionState
import com.google.android.gms.location.LocationServices
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.koin.compose.koinInject
import pt.isel.keepmyplanet.data.repository.DefaultGeocodingRepository

@OptIn(ExperimentalPermissionsApi::class)
@SuppressLint("MissingPermission")
@Composable
actual fun rememberLocationProvider(
    onLocationUpdated: (latitude: Double, longitude: Double) -> Unit,
    onLocationError: () -> Unit,
): LocationProvider {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val locationClient = remember { LocationServices.getFusedLocationProviderClient(context) }
    val onLocationUpdatedState by rememberUpdatedState(onLocationUpdated)
    val onLocationErrorState by rememberUpdatedState(onLocationError)
    val geocodingRepository: DefaultGeocodingRepository = koinInject()

    fun requestIpLocationFallback() {
        coroutineScope.launch(Dispatchers.IO) {
            fetchIpBasedLocation(
                geocodingRepository = geocodingRepository,
                onSuccess = { lat, lon -> onLocationUpdatedState(lat, lon) },
                onError = { onLocationErrorState() },
            )
        }
    }

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
                                ?: requestIpLocationFallback()
                        }.addOnFailureListener { requestIpLocationFallback() }
                } else {
                    requestIpLocationFallback()
                }
            }
        }
    }
}

actual val shouldShowUserLocationMarker: Boolean = true
