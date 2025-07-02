package pt.isel.keepmyplanet.data.repository

import pt.isel.keepmyplanet.data.api.ZoneApi
import pt.isel.keepmyplanet.data.cache.ZoneCacheRepository
import pt.isel.keepmyplanet.domain.common.Id
import pt.isel.keepmyplanet.domain.zone.Location
import pt.isel.keepmyplanet.domain.zone.Zone
import pt.isel.keepmyplanet.dto.zone.ConfirmCleanlinessRequest
import pt.isel.keepmyplanet.dto.zone.ReportZoneRequest
import pt.isel.keepmyplanet.dto.zone.UpdateZoneRequest
import pt.isel.keepmyplanet.mapper.zone.toZone

class DefaultZoneRepository(
    private val zoneApi: ZoneApi,
    private val zoneCache: ZoneCacheRepository,
) {
    suspend fun reportZone(request: ReportZoneRequest): Result<Zone> =
        zoneApi.reportZone(request).map { it.toZone() }

    suspend fun invalidateZoneCache(zoneId: Id) {
        zoneCache.deleteById(zoneId)
    }

    suspend fun getZoneDetails(
        zoneId: Id,
        forceNetwork: Boolean = false,
    ): Result<Zone> =
        runCatching {
            if (!forceNetwork) {
                val cachedZone = zoneCache.getZoneById(zoneId)
                if (cachedZone != null) return@runCatching cachedZone
            }
            val networkResult = zoneApi.getZoneDetails(zoneId.value)
            if (networkResult.isSuccess) {
                val zone = networkResult.getOrThrow().toZone()
                zoneCache.insertZones(listOf(zone))
                zone
            } else {
                zoneCache.getZoneById(zoneId) ?: throw networkResult.exceptionOrNull()!!
            }
        }

    suspend fun findZonesByLocation(
        latitude: Double,
        longitude: Double,
        radius: Double,
    ): Result<List<Zone>> =
        runCatching {
            val networkResult = zoneApi.findZonesByLocation(latitude, longitude, radius)
            if (networkResult.isSuccess) {
                val zones = networkResult.getOrThrow().map { it.toZone() }
                zoneCache.insertZones(zones)
                zones
            } else {
                zoneCache
                    .getZonesInBoundingBox(
                        min = Location(latitude - 0.1, longitude - 0.1),
                        max = Location(latitude + 0.1, longitude + 0.1),
                    ).ifEmpty { throw networkResult.exceptionOrNull()!! }
            }
        }

    suspend fun updateZone(
        zoneId: Id,
        request: UpdateZoneRequest,
    ): Result<Zone> =
        zoneApi.updateZone(zoneId.value, request).map {
            val zone = it.toZone()
            zoneCache.insertZones(listOf(zone))
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
            zoneCache.insertZones(listOf(updatedZone))
            updatedZone
        }
    }

    suspend fun revertToReported(zoneId: Id): Result<Zone> =
        zoneApi.revertToReported(zoneId.value).map { it.toZone() }
}
