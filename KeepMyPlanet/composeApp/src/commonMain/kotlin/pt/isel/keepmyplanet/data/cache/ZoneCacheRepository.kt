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
                zone.photosIds.forEach { photoId ->
                    photoQueries.insertPhoto(zone.id.value.toLong(), photoId.value.toLong())
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

    fun getZonesInBoundingBox(
        min: Location,
        max: Location,
    ): List<Zone> =
        queries
            .getZonesInBoundingBox(min.latitude, max.latitude, min.longitude, max.longitude)
            .executeAsList()
            .map { dbZone ->
                val photoIds =
                    photoQueries
                        .getPhotosByZoneId(dbZone.id)
                        .executeAsList()
                        .map { Id(it.toUInt()) }
                        .toSet()
                dbZone.toZone(photoIds)
            }

    suspend fun clearAllZones() {
        queries.clearAllZones()
    }

    fun getZoneById(id: Id): Zone? {
        val dbZone = queries.getById(id.value.toLong()).executeAsOneOrNull() ?: return null
        val photoIds =
            photoQueries
                .getPhotosByZoneId(id.value.toLong())
                .executeAsList()
                .map { Id(it.toUInt()) }
                .toSet()
        return dbZone.toZone(photoIds)
    }

    override suspend fun cleanupExpiredData(ttlSeconds: Long) {
        val expirationTime = Clock.System.now().epochSeconds - ttlSeconds
        queries.deleteExpiredZones(expirationTime)
    }
}
