package pt.isel.keepmyplanet.data.repository

import pt.isel.keepmyplanet.data.api.ZoneApi
import pt.isel.keepmyplanet.data.cache.ZoneCacheRepository
import pt.isel.keepmyplanet.domain.common.Id
import pt.isel.keepmyplanet.domain.common.ZoneDetailsBundle
import pt.isel.keepmyplanet.domain.zone.Location
import pt.isel.keepmyplanet.domain.zone.Zone
import pt.isel.keepmyplanet.dto.zone.ConfirmCleanlinessRequest
import pt.isel.keepmyplanet.dto.zone.ReportZoneRequest
import pt.isel.keepmyplanet.dto.zone.UpdateZoneRequest
import pt.isel.keepmyplanet.mapper.zone.toZone
import pt.isel.keepmyplanet.utils.calculateBoundingBox
import pt.isel.keepmyplanet.utils.haversineDistance

class ZoneApiRepository(
    private val zoneApi: ZoneApi,
    private val zoneCache: ZoneCacheRepository?,
    private val userRepository: UserApiRepository,
) {
    suspend fun reportZone(request: ReportZoneRequest): Result<Zone> =
        zoneApi.reportZone(request).map {
            val zone = it.toZone()
            zoneCache?.insertZones(listOf(zone))
            zone
        }

    suspend fun invalidateZoneCache(zoneId: Id) {
        zoneCache?.deleteById(zoneId)
    }

    suspend fun getZoneDetailsBundle(zoneId: Id): Result<ZoneDetailsBundle> =
        runCatching {
            val zone = getZoneDetails(zoneId, forceNetwork = true).getOrThrow()

            val reporterInfo = userRepository.getUserDetails(zone.reporterId).getOrThrow()

            ZoneDetailsBundle(
                zone = zone,
                reporter = reporterInfo,
            )
        }

    suspend fun getZoneDetails(
        zoneId: Id,
        forceNetwork: Boolean = false,
    ): Result<Zone> =
        runCatching {
            if (!forceNetwork) {
                val cachedZone = zoneCache?.getZoneById(zoneId)
                if (cachedZone != null) return@runCatching cachedZone
            }
            val networkResult = zoneApi.getZoneDetails(zoneId.value)
            if (networkResult.isSuccess) {
                val zone = networkResult.getOrThrow().toZone()
                zoneCache?.insertZones(listOf(zone))
                zone
            } else {
                zoneCache?.getZoneById(zoneId) ?: throw networkResult.exceptionOrNull()!!
            }
        }

    suspend fun getAllZonesFromCache(): Result<List<Zone>> =
        runCatching {
            zoneCache?.getAllZones() ?: emptyList()
        }

    suspend fun findZonesByLocationFromCache(
        latitude: Double,
        longitude: Double,
        radius: Double,
    ): Result<List<Zone>> =
        runCatching {
            val center = Location(latitude, longitude)
            val radiusInKm = radius / 1000.0
            val (minCoords, maxCoords) = calculateBoundingBox(center, radiusInKm)

            val cachedZones =
                zoneCache?.getZonesInBoundingBox(min = minCoords, max = maxCoords)?.filter {
                    haversineDistance(
                        it.location.latitude,
                        it.location.longitude,
                        latitude,
                        longitude,
                    ) <= radius
                }
            cachedZones ?: emptyList()
        }

    suspend fun findZonesByLocation(
        latitude: Double,
        longitude: Double,
        radius: Double,
    ): Result<List<Zone>> =
        runCatching {
            val radiusInKm = radius / 1000.0
            val networkResult = zoneApi.findZonesByLocation(latitude, longitude, radiusInKm)
            val zones = networkResult.getOrThrow().map { it.toZone() }
            zoneCache?.insertZones(zones)
            zones
        }.recoverCatching {
            val radiusInKm = radius / 1000.0
            val (minCoords, maxCoords) =
                calculateBoundingBox(
                    Location(latitude, longitude),
                    radiusInKm,
                )
            val cachedZones =
                zoneCache?.getZonesInBoundingBox(min = minCoords, max = maxCoords)?.filter {
                    haversineDistance(
                        it.location.latitude,
                        it.location.longitude,
                        latitude,
                        longitude,
                    ) <=
                        radius
                }
            cachedZones ?: throw it
        }

    suspend fun updateZone(
        zoneId: Id,
        request: UpdateZoneRequest,
    ): Result<Zone> =
        zoneApi.updateZone(zoneId.value, request).map {
            val zone = it.toZone()
            zoneCache?.insertZones(listOf(zone))
            zone
        }

    suspend fun addPhotoToZone(
        zoneId: Id,
        photoId: Id,
        type: String,
    ): Result<Zone> =
        zoneApi.addPhotoToZone(zoneId.value, photoId.value, type).map {
            val zone = it.toZone()
            zoneCache?.insertZones(listOf(zone))
            zone
        }

    suspend fun removePhotoFromZone(
        zoneId: Id,
        photoId: Id,
    ): Result<Zone> =
        zoneApi.removePhotoFromZone(zoneId.value, photoId.value).map {
            val zone = it.toZone()
            zoneCache?.insertZones(listOf(zone))
            zone
        }

    suspend fun deleteZone(zoneId: Id): Result<Unit> = zoneApi.deleteZone(zoneId.value)

    suspend fun confirmCleanliness(
        zoneId: Id,
        eventId: Id,
        wasCleaned: Boolean,
    ): Result<Zone> {
        val request =
            ConfirmCleanlinessRequest(
                wasCleaned = wasCleaned,
                eventId = eventId.value,
            )
        return zoneApi.confirmCleanliness(zoneId.value, request).map {
            val updatedZone = it.toZone()
            zoneCache?.insertZones(listOf(updatedZone))
            updatedZone
        }
    }

    suspend fun revertToReported(zoneId: Id): Result<Zone> =
        zoneApi.revertToReported(zoneId.value).map { it.toZone() }
}
