package pt.isel.keepmyplanet.data.cache

import kotlinx.datetime.Clock
import pt.isel.keepmyplanet.cache.KeepMyPlanetCache

class MapTileCacheRepository(
    database: KeepMyPlanetCache,
) {
    private val queries = database.mapTileCacheQueries

    fun getTile(
        zoom: Int,
        col: Int,
        row: Int,
    ): ByteArray? = queries.getTile(zoom.toLong(), col.toLong(), row.toLong()).executeAsOneOrNull()

    suspend fun insertTile(
        zoom: Int,
        col: Int,
        row: Int,
        data: ByteArray,
    ) {
        queries.insertTile(
            zoom_level = zoom.toLong(),
            col = col.toLong(),
            row = row.toLong(),
            image_data = data,
            timestamp = Clock.System.now().epochSeconds,
        )
    }

    suspend fun deleteExpiredTiles(ttlSeconds: Long) {
        val expirationTime = Clock.System.now().epochSeconds - ttlSeconds
        queries.deleteExpiredTiles(expirationTime)
    }
}
