package pt.isel.keepmyplanet.ui.components

import pt.isel.keepmyplanet.data.repository.GeocodingApiRepository

internal suspend fun fetchIpBasedLocation(
    geocodingRepository: GeocodingApiRepository,
    onSuccess: (latitude: Double, longitude: Double) -> Unit,
    onError: () -> Unit,
) {
    geocodingRepository
        .getIpLocation()
        .onSuccess { response ->
            val (latStr, lonStr) = response.loc.split(',')
            val lat = latStr.toDoubleOrNull()
            val lon = lonStr.toDoubleOrNull()
            if (lat != null && lon != null) {
                onSuccess(lat, lon)
            } else {
                onError()
            }
        }.onFailure {
            println("Could not fetch IP-based location via proxy: ${it.message}")
            onError()
        }
}
