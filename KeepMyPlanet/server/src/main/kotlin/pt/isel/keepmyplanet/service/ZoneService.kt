package pt.isel.keepmyplanet.service

import pt.isel.keepmyplanet.domain.common.Description
import pt.isel.keepmyplanet.domain.common.Id
import pt.isel.keepmyplanet.domain.common.Location
import pt.isel.keepmyplanet.domain.user.User
import pt.isel.keepmyplanet.domain.zone.Zone
import pt.isel.keepmyplanet.domain.zone.ZoneSeverity
import pt.isel.keepmyplanet.domain.zone.ZoneStatus
import pt.isel.keepmyplanet.errors.AuthorizationException
import pt.isel.keepmyplanet.errors.ConflictException
import pt.isel.keepmyplanet.errors.InternalServerException
import pt.isel.keepmyplanet.errors.NotFoundException
import pt.isel.keepmyplanet.errors.ValidationException
import pt.isel.keepmyplanet.repository.EventRepository
import pt.isel.keepmyplanet.repository.UserRepository
import pt.isel.keepmyplanet.repository.ZoneRepository
import pt.isel.keepmyplanet.util.now

class ZoneService(
    private val zoneRepository: ZoneRepository,
    private val userRepository: UserRepository,
    private val eventRepository: EventRepository,
) {
    suspend fun reportZone(
        location: Location,
        description: Description,
        photosIds: Set<Id>,
        reporterId: Id,
        zoneSeverity: ZoneSeverity = ZoneSeverity.UNKNOWN,
    ): Result<Zone> =
        runCatching {
            findUserOrFail(reporterId)

            val currentTime = now()
            val newZone =
                Zone(
                    id = Id(0U),
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
            if (radius <= 0) throw ValidationException("Radius must be positive.")
            zoneRepository.findNearLocation(center, radius)
        }

    suspend fun updateZone(
        zoneId: Id,
        userId: Id,
        description: Description? = null,
        status: ZoneStatus? = null,
        severity: ZoneSeverity? = null,
    ): Result<Zone> =
        runCatching {
            val zone = findZoneOrFail(zoneId)
            hasPermissionsOrFail(zone, userId)

            var modifiedZone = zone
            var hasChanges = false

            description?.let { newDescription ->
                if (modifiedZone.description != newDescription) {
                    modifiedZone = modifiedZone.copy(description = newDescription)
                    hasChanges = true
                }
            }

            status?.let { newStatus ->
                if (modifiedZone.status != newStatus) {
                    modifiedZone = modifiedZone.copy(status = newStatus)
                    hasChanges = true
                }
            }

            severity?.let { newSeverity ->
                if (modifiedZone.zoneSeverity != newSeverity) {
                    modifiedZone = modifiedZone.copy(zoneSeverity = newSeverity)
                    hasChanges = true
                }
            }

            if (hasChanges) zoneRepository.update(modifiedZone) else zone
        }

    suspend fun addPhotoToZone(
        zoneId: Id,
        userId: Id,
        photoId: Id,
    ): Result<Zone> =
        runCatching {
            val zone = findZoneOrFail(zoneId)
            hasPermissionsOrFail(zone, userId)

            if (photoId in zone.photosIds) return@runCatching zone

            val updatedZone = zone.copy(photosIds = zone.photosIds + photoId)
            zoneRepository.update(updatedZone)
        }

    suspend fun removePhotoFromZone(
        zoneId: Id,
        userId: Id,
        photoId: Id,
    ): Result<Zone> =
        runCatching {
            val zone = findZoneOrFail(zoneId)
            hasPermissionsOrFail(zone, userId)

            if (photoId !in zone.photosIds) {
                throw NotFoundException("Photo '$photoId' not found in zone '$zoneId'.")
            }

            val updatedZone = zone.copy(photosIds = zone.photosIds - photoId)
            zoneRepository.update(updatedZone)
        }

    suspend fun deleteZone(
        zoneId: Id,
        userId: Id,
    ): Result<Unit> =
        runCatching {
            val zone = findZoneOrFail(zoneId)
            hasPermissionsOrFail(zone, userId)

            if (zone.eventId != null && zone.status == ZoneStatus.CLEANING_SCHEDULED) {
                throw ConflictException(
                    "Cannot delete zone '$zoneId' linked to a scheduled event.",
                )
            }

            val deleted = zoneRepository.deleteById(zoneId)
            if (!deleted) throw InternalServerException("Failed to delete zone $zoneId.")
            Unit
        }

    private suspend fun findZoneOrFail(zoneId: Id): Zone =
        zoneRepository.getById(zoneId)
            ?: throw NotFoundException("Zone '$zoneId' not found.")

    private suspend fun findUserOrFail(userId: Id): User =
        userRepository.getById(userId)
            ?: throw NotFoundException("User '$userId' not found.")

    private suspend fun hasPermissionsOrFail(
        zone: Zone,
        userId: Id,
    ) {
        findUserOrFail(userId)
        when (val eventId = zone.eventId) {
            null ->
                if (userId != zone.reporterId) {
                    throw AuthorizationException("User '$userId' is not reporter.")
                }

            else -> {
                val event =
                    eventRepository.getById(eventId)
                        ?: throw NotFoundException("Event '$eventId' not found.")
                if (userId != event.organizerId) {
                    throw AuthorizationException("User '$userId' is not event organizer.")
                }
            }
        }
    }
}
