package pt.isel.keepmyplanet.services

import pt.isel.keepmyplanet.core.NotFoundException
import pt.isel.keepmyplanet.core.PhotoNotFoundInZoneException
import pt.isel.keepmyplanet.core.ZoneInvalidStateException
import pt.isel.keepmyplanet.core.ZoneUpdateException
import pt.isel.keepmyplanet.domain.common.Description
import pt.isel.keepmyplanet.domain.common.Id
import pt.isel.keepmyplanet.domain.common.Location
import pt.isel.keepmyplanet.domain.zone.Zone
import pt.isel.keepmyplanet.domain.zone.ZoneSeverity
import pt.isel.keepmyplanet.domain.zone.ZoneStatus
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
            zoneRepository.getById(zoneId) ?: throw NotFoundException("Zone", zoneId)
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
            val zone = zoneRepository.getById(zoneId) ?: throw NotFoundException("Zone", zoneId)

            if (!isValidStatusTransition(zone.status, newStatus)) {
                throw ZoneInvalidStateException(
                    "Cannot transition zone $zoneId from ${zone.status} to $newStatus.",
                )
            }

            val updatedZone = zone.copy(status = newStatus, updatedAt = now())
            zoneRepository.update(updatedZone)
        }

    suspend fun updateZoneSeverity(
        zoneId: Id,
        newZoneSeverity: ZoneSeverity,
    ): Result<Zone> =
        runCatching {
            val zone = zoneRepository.getById(zoneId) ?: throw NotFoundException("Zone", zoneId)
            val updatedZone = zone.copy(zoneSeverity = newZoneSeverity, updatedAt = now())
            zoneRepository.update(updatedZone)
        }

    suspend fun addPhotoToZone(
        zoneId: Id,
        photoId: Id,
    ): Result<Zone> =
        runCatching {
            val zone = zoneRepository.getById(zoneId) ?: throw NotFoundException("Zone", zoneId)

            val updatedPhotosIds = zone.photosIds + photoId
            if (updatedPhotosIds.size == zone.photosIds.size && photoId in zone.photosIds) {
                return@runCatching zone
            }

            val updatedZone = zone.copy(photosIds = updatedPhotosIds, updatedAt = now())
            zoneRepository.update(updatedZone)
        }

    suspend fun removePhotoFromZone(
        zoneId: Id,
        photoId: Id,
    ): Result<Zone> =
        runCatching {
            val zone = zoneRepository.getById(zoneId) ?: throw NotFoundException("Zone", zoneId)

            if (photoId !in zone.photosIds) {
                throw PhotoNotFoundInZoneException(zoneId, photoId)
            }

            val updatedPhotosIds = zone.photosIds - photoId
            val updatedZone = zone.copy(photosIds = updatedPhotosIds, updatedAt = now())
            zoneRepository.update(updatedZone)
        }

    suspend fun deleteZone(zoneId: Id): Result<Unit> =
        runCatching {
            val zone = zoneRepository.getById(zoneId) ?: throw NotFoundException("Zone", zoneId)

            if (zone.eventId != null && zone.status == ZoneStatus.CLEANING_SCHEDULED) {
                throw ZoneInvalidStateException(
                    "Cannot delete zone $zoneId linked to an active event.",
                )
            }

            val deleted = zoneRepository.deleteById(zoneId)
            if (!deleted) throw ZoneUpdateException("Failed to delete zone $zoneId.")
        }

    private fun isValidStatusTransition(
        from: ZoneStatus,
        to: ZoneStatus,
    ): Boolean {
        if (from == to) return false
        return when (from) {
            ZoneStatus.REPORTED -> to in setOf(ZoneStatus.CLEANING_SCHEDULED)
            ZoneStatus.CLEANING_SCHEDULED -> to in setOf(ZoneStatus.CLEANED, ZoneStatus.REPORTED)
            ZoneStatus.CLEANED -> to in setOf(ZoneStatus.REPORTED)
        }
    }
}
