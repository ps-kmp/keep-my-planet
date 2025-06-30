package pt.isel.keepmyplanet.data.api

import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get
import io.ktor.http.path
import pt.isel.keepmyplanet.data.repository.GeocodingCacheRepository
import pt.isel.keepmyplanet.domain.common.Place
import pt.isel.keepmyplanet.dto.geocoding.NominatimResult
import pt.isel.keepmyplanet.mapper.geocoding.toPlace

class GeocodingApi(
    private val httpClient: HttpClient,
    private val cacheRepository: GeocodingCacheRepository,
) {
    suspend fun search(query: String): Result<List<Place>> =
        runCatching {
            if (query.isBlank() || query.length < 3) return@runCatching emptyList()

            val cachedResult = cacheRepository.getResult(query)
            if (cachedResult != null) return@runCatching cachedResult

            val response: List<NominatimResult> =
                httpClient
                    .get("https://nominatim.openstreetmap.org") {
                        url {
                            path("search")
                            parameters.append("q", query)
                            parameters.append("format", "jsonv2")
                            parameters.append("addressdetails", "1")
                            parameters.append("limit", "5")
                        }
                    }.body()

            val places = response.map { it.toPlace() }
            cacheRepository.insertResult(query, places)
            places
        }
}
