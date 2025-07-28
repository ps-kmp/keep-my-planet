package pt.isel.keepmyplanet.ui.components

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.rememberUpdatedState
import kotlinx.coroutines.launch
import org.koin.compose.koinInject
import pt.isel.keepmyplanet.data.repository.GeocodingApiRepository

@Composable
actual fun rememberLocationProvider(
    onLocationUpdated: (latitude: Double, longitude: Double) -> Unit,
    onLocationError: () -> Unit,
): LocationProvider {
    val onLocationUpdatedState by rememberUpdatedState(onLocationUpdated)
    val onLocationErrorState by rememberUpdatedState(onLocationError)
    val geocodingRepository: GeocodingApiRepository = koinInject()
    val scope = rememberCoroutineScope()

    return remember {
        object : LocationProvider {
            override val isPermissionGranted: Boolean = true

            override fun requestPermission() {}

            override fun requestLocationUpdate() {
                scope.launch {
                    fetchIpBasedLocation(
                        geocodingRepository = geocodingRepository,
                        onSuccess = { lat, lon -> onLocationUpdatedState(lat, lon) },
                        onError = { onLocationErrorState() },
                    )
                }
            }
        }
    }
}

actual val shouldShowUserLocationMarker: Boolean = false
