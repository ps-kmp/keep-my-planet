package pt.isel.keepmyplanet.service

import pt.isel.keepmyplanet.domain.common.Description
import pt.isel.keepmyplanet.domain.common.Id
import pt.isel.keepmyplanet.domain.common.Location
import pt.isel.keepmyplanet.domain.zone.Zone
import pt.isel.keepmyplanet.domain.zone.ZoneSeverity
import pt.isel.keepmyplanet.domain.zone.ZoneStatus
import pt.isel.keepmyplanet.errors.ConflictException
import pt.isel.keepmyplanet.errors.InternalServerException
import pt.isel.keepmyplanet.errors.NotFoundException
import pt.isel.keepmyplanet.repository.ZoneRepository
import pt.isel.keepmyplanet.util.now

class ZoneService(
    private val zoneRepository: ZoneRepository,
) {
    suspend fun reportZone(
        location: Location,
        description: Description,
        photosIds: Set<Id>,
        reporterId: Id,
        zoneSeverity: ZoneSeverity = ZoneSeverity.UNKNOWN,
    ): Result<Zone> =
        runCatching {
            val currentTime = now()
            val newZone =
                Zone(
                    id = Id(1U),
                    location = location,
                    description = description,
                    reporterId = reporterId,
                    photosIds = photosIds,
                    zoneSeverity = zoneSeverity,
                    createdAt = currentTime,
                    updatedAt = currentTime,
                )
            zoneRepository.create(newZone)
        }

    suspend fun getZoneDetails(zoneId: Id): Result<Zone> =
        runCatching {
            findZoneOrFail(zoneId)
        }

    suspend fun findAll(): Result<List<Zone>> =
        runCatching {
            zoneRepository.getAll()
        }

    suspend fun findZones(
        center: Location,
        radius: Double,
    ): Result<List<Zone>> =
        runCatching {
            zoneRepository.findNearLocation(center, radius)
        }

    suspend fun updateZoneStatus(
        zoneId: Id,
        newStatus: ZoneStatus,
    ): Result<Zone> =
        runCatching {
            val zone = findZoneOrFail(zoneId)
            if (zone.status == newStatus) return@runCatching zone
            val updatedZone = zone.copy(status = newStatus, updatedAt = now())
            zoneRepository.update(updatedZone)
        }

    suspend fun updateZoneSeverity(
        zoneId: Id,
        newZoneSeverity: ZoneSeverity,
    ): Result<Zone> =
        runCatching {
            val zone = findZoneOrFail(zoneId)
            if (zone.zoneSeverity == newZoneSeverity) return@runCatching zone
            val updatedZone = zone.copy(zoneSeverity = newZoneSeverity, updatedAt = now())
            zoneRepository.update(updatedZone)
        }

    suspend fun addPhotoToZone(
        zoneId: Id,
        photoId: Id,
    ): Result<Zone> =
        runCatching {
            val zone = findZoneOrFail(zoneId)
            if (photoId in zone.photosIds) return@runCatching zone
            val updatedZone = zone.copy(photosIds = zone.photosIds + photoId, updatedAt = now())
            zoneRepository.update(updatedZone)
        }

    suspend fun removePhotoFromZone(
        zoneId: Id,
        photoId: Id,
    ): Result<Zone> =
        runCatching {
            val zone = findZoneOrFail(zoneId)

            if (photoId !in zone.photosIds) {
                throw NotFoundException("Photo '$photoId' not found in zone '$zoneId'.")
            }

            val updatedZone = zone.copy(photosIds = zone.photosIds - photoId, updatedAt = now())
            zoneRepository.update(updatedZone)
        }

    suspend fun deleteZone(zoneId: Id): Result<Unit> =
        runCatching {
            val zone = findZoneOrFail(zoneId)

            if (zone.eventId != null && zone.status == ZoneStatus.CLEANING_SCHEDULED) {
                throw ConflictException(
                    "Cannot delete zone '$zoneId' linked to a scheduled event.",
                )
            }

            val deleted = zoneRepository.deleteById(zoneId)
            if (!deleted) throw InternalServerException("Failed to delete zone $zoneId.")
        }

    private suspend fun findZoneOrFail(zoneId: Id): Zone =
        zoneRepository.getById(zoneId)
            ?: throw NotFoundException("Zone '$zoneId' not found.")
}
