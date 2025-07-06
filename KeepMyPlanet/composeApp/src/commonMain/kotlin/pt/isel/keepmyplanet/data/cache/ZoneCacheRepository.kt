package pt.isel.keepmyplanet.data.cache

import kotlinx.datetime.Clock
import pt.isel.keepmyplanet.cache.KeepMyPlanetCache
import pt.isel.keepmyplanet.data.cache.mappers.toZone
import pt.isel.keepmyplanet.domain.common.Id
import pt.isel.keepmyplanet.domain.zone.Location
import pt.isel.keepmyplanet.domain.zone.Zone

class ZoneCacheRepository(
    database: KeepMyPlanetCache,
) : CleanableCache {
    private val queries = database.zoneCacheQueries
    private val photoQueries = database.zonePhotosCacheQueries

    suspend fun insertZones(zones: List<Zone>) {
        queries.transaction {
            zones.forEach { zone ->
                queries.insertZone(
                    id = zone.id.value.toLong(),
                    latitude = zone.location.latitude,
                    longitude = zone.location.longitude,
                    description = zone.description.value,
                    reporterId = zone.reporterId.value.toLong(),
                    eventId = zone.eventId?.value?.toLong(),
                    status = zone.status.name,
                    zoneSeverity = zone.zoneSeverity.name,
                    createdAt = zone.createdAt.toString(),
                    updatedAt = zone.updatedAt.toString(),
                    timestamp = Clock.System.now().epochSeconds,
                )
                zone.beforePhotosIds.forEach { photoId ->
                    photoQueries.insertPhoto(
                        zone.id.value.toLong(),
                        photoId.value.toLong(),
                        "BEFORE",
                    )
                }
                zone.afterPhotosIds.forEach { photoId ->
                    photoQueries.insertPhoto(
                        zone.id.value.toLong(),
                        photoId.value.toLong(),
                        "AFTER",
                    )
                }
            }
        }
    }

    suspend fun deleteById(id: Id) {
        queries.transaction {
            photoQueries.deletePhotosByZoneId(id.value.toLong())
            queries.deleteById(id.value.toLong())
        }
    }

    suspend fun getZonesInBoundingBox(
        min: Location,
        max: Location,
    ): List<Zone> =
        queries
            .getZonesInBoundingBox(min.latitude, max.latitude, min.longitude, max.longitude)
            .executeAsList()
            .map { dbZone ->
                val beforePhotoIds =
                    photoQueries
                        .getBeforePhotosByZoneId(dbZone.id)
                        .executeAsList()
                        .map { Id(it.toUInt()) }
                        .toSet()
                val afterPhotoIds =
                    photoQueries
                        .getAfterPhotosByZoneId(dbZone.id)
                        .executeAsList()
                        .map { Id(it.toUInt()) }
                        .toSet()
                dbZone.toZone(beforePhotoIds, afterPhotoIds)
            }

    suspend fun clearAllZones() {
        queries.clearAllZones()
    }

    suspend fun getZoneById(id: Id): Zone? {
        val dbZone = queries.getById(id.value.toLong()).executeAsOneOrNull() ?: return null
        val beforePhotoIds =
            photoQueries
                .getBeforePhotosByZoneId(id.value.toLong())
                .executeAsList()
                .map { Id(it.toUInt()) }
                .toSet()
        val afterPhotoIds =
            photoQueries
                .getAfterPhotosByZoneId(id.value.toLong())
                .executeAsList()
                .map { Id(it.toUInt()) }
                .toSet()
        return dbZone.toZone(beforePhotoIds, afterPhotoIds)
    }

    override suspend fun cleanupExpiredData(ttlSeconds: Long) {
        val expirationTime = Clock.System.now().epochSeconds - ttlSeconds
        queries.deleteExpiredZones(expirationTime)
    }
}
