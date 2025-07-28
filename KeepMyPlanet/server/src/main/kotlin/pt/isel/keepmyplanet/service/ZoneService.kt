package pt.isel.keepmyplanet.service

import kotlin.time.Duration.Companion.hours
import pt.isel.keepmyplanet.domain.common.Description
import pt.isel.keepmyplanet.domain.common.Id
import pt.isel.keepmyplanet.domain.event.EventStatus
import pt.isel.keepmyplanet.domain.user.User
import pt.isel.keepmyplanet.domain.zone.Location
import pt.isel.keepmyplanet.domain.zone.Radius
import pt.isel.keepmyplanet.domain.zone.Zone
import pt.isel.keepmyplanet.domain.zone.ZoneSeverity
import pt.isel.keepmyplanet.domain.zone.ZoneStatus
import pt.isel.keepmyplanet.exception.AuthorizationException
import pt.isel.keepmyplanet.exception.ConflictException
import pt.isel.keepmyplanet.exception.InternalServerException
import pt.isel.keepmyplanet.exception.NotFoundException
import pt.isel.keepmyplanet.exception.ValidationException
import pt.isel.keepmyplanet.repository.EventRepository
import pt.isel.keepmyplanet.repository.PhotoRepository
import pt.isel.keepmyplanet.repository.UserRepository
import pt.isel.keepmyplanet.repository.ZoneRepository
import pt.isel.keepmyplanet.utils.AuthPrincipal
import pt.isel.keepmyplanet.utils.minus
import pt.isel.keepmyplanet.utils.now

class ZoneService(
    private val zoneRepository: ZoneRepository,
    private val userRepository: UserRepository,
    private val eventRepository: EventRepository,
    private val photoRepository: PhotoRepository,
    private val zoneStateChangeService: ZoneStateChangeService,
) {
    suspend fun reportZone(
        location: Location,
        description: Description,
        radius: Radius,
        photosIds: Set<Id>,
        reporterId: Id,
        zoneSeverity: ZoneSeverity = ZoneSeverity.UNKNOWN,
    ): Result<Zone> =
        runCatching {
            findUserOrFail(reporterId)

            photosIds.forEach { photoId ->
                val photo =
                    photoRepository.getById(photoId)
                        ?: throw ValidationException("Photo with ID '$photoId' not found.")
                if (photo.uploaderId != reporterId) {
                    throw AuthorizationException(
                        "Cannot use photo '$photoId' as it was not uploaded by you.",
                    )
                }
            }

            val currentTime = now()
            val newZone =
                Zone(
                    id = Id(0U),
                    location = location,
                    radius = radius,
                    description = description,
                    reporterId = reporterId,
                    beforePhotosIds = photosIds,
                    afterPhotosIds = emptySet(),
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
        actingPrincipal: AuthPrincipal,
        description: Description? = null,
        radius: Radius? = null,
        status: ZoneStatus? = null,
        severity: ZoneSeverity? = null,
    ): Result<Zone> =
        runCatching {
            val zone = findZoneOrFail(zoneId)
            ensureManagementPermissions(zone, actingPrincipal)

            var modifiedZone = zone
            var hasChanges = false

            description?.let { newDescription ->
                if (modifiedZone.description != newDescription) {
                    modifiedZone = modifiedZone.copy(description = newDescription)
                    hasChanges = true
                }
            }

            radius?.let { newRadius ->
                if (modifiedZone.radius != newRadius) {
                    modifiedZone = modifiedZone.copy(radius = newRadius)
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
        actingPrincipal: AuthPrincipal,
        photoId: Id,
        photoType: String,
    ): Result<Zone> =
        runCatching {
            val zone = findZoneOrFail(zoneId)
            ensureManagementPermissions(zone, actingPrincipal)

            val photo =
                photoRepository.getById(photoId) // This is a validation
                    ?: throw ValidationException("Photo with ID '$photoId' not found.")
            if (photo.uploaderId != actingPrincipal.id) {
                throw AuthorizationException(
                    "Cannot use photo '$photoId' as it was not uploaded by you.",
                )
            }

            val type = photoType.uppercase()
            if (type != "BEFORE" && type != "AFTER") {
                throw ValidationException("Photo type must be 'BEFORE' or 'AFTER'")
            }

            if (type == "AFTER" && zone.status != ZoneStatus.CLEANED) {
                throw ConflictException(
                    "Can only add 'AFTER' photos to a zone that has been marked as 'CLEANED'.",
                )
            }

            if (photoId in zone.beforePhotosIds ||
                photoId in zone.afterPhotosIds
            ) {
                return@runCatching zone
            }

            val updatedZone =
                if (type == "AFTER") {
                    zone.copy(afterPhotosIds = zone.afterPhotosIds + photoId)
                } else {
                    zone.copy(beforePhotosIds = zone.beforePhotosIds + photoId)
                }
            zoneRepository.update(updatedZone)
        }

    suspend fun removePhotoFromZone(
        zoneId: Id,
        actingPrincipal: AuthPrincipal,
        photoId: Id,
    ): Result<Zone> =
        runCatching {
            val zone = findZoneOrFail(zoneId)
            ensureManagementPermissions(zone, actingPrincipal)

            if (photoId !in zone.beforePhotosIds && photoId !in zone.afterPhotosIds) {
                throw NotFoundException("Photo '$photoId' not found in zone '$zoneId'.")
            }

            val updatedZone =
                zone.copy(
                    beforePhotosIds = zone.beforePhotosIds - photoId,
                    afterPhotosIds = zone.afterPhotosIds - photoId,
                )
            zoneRepository.update(updatedZone)
        }

    suspend fun deleteZone(
        zoneId: Id,
        actingPrincipal: AuthPrincipal,
    ): Result<Unit> =
        runCatching {
            val zone = findZoneOrFail(zoneId)
            ensureManagementPermissions(zone, actingPrincipal)

            if (zone.eventId != null && zone.status == ZoneStatus.CLEANING_SCHEDULED) {
                throw ConflictException(
                    "Cannot delete zone '$zoneId' linked to a scheduled event.",
                )
            }

            val deleted = zoneRepository.deleteById(zoneId)
            if (!deleted) throw InternalServerException("Failed to delete zone $zoneId.")
            Unit
        }

    suspend fun confirmZoneCleanliness(
        zoneId: Id,
        organizerId: Id,
        wasCleaned: Boolean,
        eventId: Id,
        newSeverity: ZoneSeverity? = null,
    ): Result<Zone> =
        runCatching {
            val zone = findZoneOrFail(zoneId)
            val event =
                eventRepository.getById(eventId)
                    ?: throw NotFoundException("Triggering event '$eventId' not found.")

            if (event.organizerId != organizerId) {
                throw AuthorizationException("Only the event organizer can confirm zone status.")
            }

            if (event.status != EventStatus.COMPLETED) {
                throw ConflictException(
                    "Cannot confirm cleanliness for an event that is not 'COMPLETED'.",
                )
            }

            if (wasCleaned) {
                if (zone.status != ZoneStatus.CLEANED) {
                    val updatedZone =
                        zoneStateChangeService.changeZoneStatus(
                            zone = zone,
                            newStatus = ZoneStatus.CLEANED,
                            changedBy = organizerId,
                            triggeredByEventId = eventId,
                        )
                    val archivedZone = zoneStateChangeService.archiveZone(updatedZone)
                    zoneRepository.update(archivedZone)
                } else {
                    zone
                }
            } else {
                var zoneToUpdate =
                    zoneStateChangeService.changeZoneStatus(
                        zone = zone,
                        newStatus = ZoneStatus.REPORTED,
                        changedBy = organizerId,
                        triggeredByEventId = eventId,
                    )
                newSeverity?.let {
                    if (zoneToUpdate.zoneSeverity != it) {
                        zoneToUpdate = zoneToUpdate.copy(zoneSeverity = it)
                    }
                }
                zoneRepository.update(zoneToUpdate.copy(eventId = null))
            }
        }

    suspend fun revertZoneToReported(
        zoneId: Id,
        userId: Id,
    ): Result<Zone> =
        runCatching {
            val zone = findZoneOrFail(zoneId)
            zoneStateChangeService.changeZoneStatus(
                zone = zone,
                newStatus = ZoneStatus.REPORTED,
                changedBy = userId,
                triggeredByEventId = null,
            )
        }

    suspend fun processZoneConfirmationTimeouts() {
        val timeThreshold = now().minus(24.hours)
        val eventsToProcess = eventRepository.findCompletedEventsPendingConfirmation(timeThreshold)

        if (eventsToProcess.isEmpty()) {
            println("Timeout Job: No zones to process.")
            return
        }

        println("Timeout Job: Found ${eventsToProcess.size} zones to process.")

        for (event in eventsToProcess) {
            val zone = zoneRepository.getById(event.zoneId)
            if (zone == null) {
                println(
                    "Timeout Job: Zone ${event.zoneId} for event ${event.id} not found. Skipping.",
                )
                continue
            }

            if (zone.status == ZoneStatus.CLEANING_SCHEDULED) {
                println(
                    "Timeout Job: Processing zone ${zone.id}. Setting to CLEANED and archiving.",
                )

                val updatedZone =
                    zoneStateChangeService.changeZoneStatus(
                        zone = zone,
                        newStatus = ZoneStatus.CLEANED,
                        changedBy = null,
                        triggeredByEventId = event.id,
                    )

                zoneStateChangeService.archiveZone(updatedZone)
            }
        }
    }

    private suspend fun findZoneOrFail(zoneId: Id): Zone =
        zoneRepository.getById(zoneId)
            ?: throw NotFoundException("Zone '$zoneId' not found.")

    private suspend fun findUserOrFail(userId: Id): User =
        userRepository.getById(userId)
            ?: throw NotFoundException("User '$userId' not found.")

    private suspend fun ensureManagementPermissions(
        zone: Zone,
        principal: AuthPrincipal,
    ) {
        val isReporter = zone.reporterId == principal.id
        val isEventOrganizer =
            zone.eventId?.let { eventId ->
                eventRepository.getById(eventId)?.organizerId == principal.id
            } ?: false
        val isAdmin = principal.role == pt.isel.keepmyplanet.domain.user.UserRole.ADMIN

        if (!isReporter && !isEventOrganizer && !isAdmin) {
            throw AuthorizationException(
                "User '${principal.id}' is not authorized to manage this zone.",
            )
        }
    }
}
