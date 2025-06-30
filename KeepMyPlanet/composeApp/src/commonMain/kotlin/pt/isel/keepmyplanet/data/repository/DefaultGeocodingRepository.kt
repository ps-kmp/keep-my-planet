package pt.isel.keepmyplanet.data.repository

import pt.isel.keepmyplanet.data.api.GeocodingApi
import pt.isel.keepmyplanet.domain.common.Place

class DefaultGeocodingRepository(
    private val geocodingApi: GeocodingApi,
) {
    suspend fun search(query: String): Result<List<Place>> = geocodingApi.search(query)
}
