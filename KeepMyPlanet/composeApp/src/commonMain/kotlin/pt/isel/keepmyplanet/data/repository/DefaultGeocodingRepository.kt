package pt.isel.keepmyplanet.data.repository

import pt.isel.keepmyplanet.data.api.GeocodingApi
import pt.isel.keepmyplanet.domain.common.Place
import pt.isel.keepmyplanet.dto.geocoding.IpLocationResponse

class DefaultGeocodingRepository(
    private val geocodingApi: GeocodingApi,
) {
    suspend fun search(query: String): Result<List<Place>> = geocodingApi.search(query)

    suspend fun getIpLocation(): Result<IpLocationResponse> = geocodingApi.getIpLocation()
}
