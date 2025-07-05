package pt.isel.keepmyplanet.data.cache

import kotlinx.datetime.Clock
import kotlinx.serialization.json.Json
import pt.isel.keepmyplanet.cache.KeepMyPlanetCache
import pt.isel.keepmyplanet.domain.common.Place

class GeocodingCacheRepository(
    database: KeepMyPlanetCache,
) : CleanableCache {
    private val queries = database.geocodingCacheQueries

    fun getResult(query: String): List<Place>? {
        val json = queries.getResultByQuery(query).executeAsOneOrNull() ?: return null
        return Json.decodeFromString<List<Place>>(json)
    }

    suspend fun insertResult(
        query: String,
        results: List<Place>,
    ) {
        queries.insertResult(
            query = query,
            results_json = Json.encodeToString(results),
            timestamp = Clock.System.now().epochSeconds,
        )
    }

    override suspend fun cleanupExpiredData(ttlSeconds: Long) {
        val expirationTime = Clock.System.now().epochSeconds - ttlSeconds
        queries.deleteExpiredResults(expirationTime)
    }
}
