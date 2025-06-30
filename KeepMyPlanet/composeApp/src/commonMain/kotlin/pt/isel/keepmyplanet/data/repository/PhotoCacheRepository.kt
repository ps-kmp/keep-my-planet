package pt.isel.keepmyplanet.data.repository

import kotlinx.datetime.Clock
import pt.isel.keepmyplanet.cache.KeepMyPlanetCache
import pt.isel.keepmyplanet.domain.common.Id

class PhotoCacheRepository(
    database: KeepMyPlanetCache,
) {
    private val queries = database.photoCacheQueries

    suspend fun insertPhotoUrl(
        id: Id,
        url: String,
    ) {
        queries.insertPhotoUrl(
            id = id.value.toLong(),
            url = url,
            id_ = id.value.toLong(),
            timestamp = Clock.System.now().epochSeconds,
        )
    }

    suspend fun updatePhotoData(
        id: Id,
        data: ByteArray,
    ) {
        queries.updatePhotoData(data, id.value.toLong())
    }

    fun getPhotoData(id: Id): ByteArray? =
        queries.getPhotoDataById(id.value.toLong()).executeAsOneOrNull()?.image_data

    fun getPhotoUrl(id: Id): String? =
        queries.getPhotoUrlById(id.value.toLong()).executeAsOneOrNull()

    suspend fun deleteExpiredPhotos(ttlSeconds: Long) {
        val expirationTime = Clock.System.now().epochSeconds - ttlSeconds
        queries.deleteExpiredPhotos(expirationTime)
    }
}
