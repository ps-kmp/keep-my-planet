package pt.isel.keepmyplanet.repository.mem

import kotlinx.datetime.LocalDateTime
import pt.isel.keepmyplanet.domain.common.Id
import pt.isel.keepmyplanet.domain.common.Location
import pt.isel.keepmyplanet.domain.zone.Severity
import pt.isel.keepmyplanet.domain.zone.Zone
import pt.isel.keepmyplanet.domain.zone.ZoneStatus
import pt.isel.keepmyplanet.repository.ZoneRepository
import pt.isel.keepmyplanet.services.PhotoAlreadyAddedException
import pt.isel.keepmyplanet.services.PhotoAssociatedWithDifferentZoneException
import pt.isel.keepmyplanet.services.PhotoNotAssociatedException
import pt.isel.keepmyplanet.services.ZoneNotFoundException
import pt.isel.keepmyplanet.utils.nowUTC
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.atomic.AtomicInteger

class InMemoryZoneRepository : ZoneRepository {
    private val zonesById = ConcurrentHashMap<Id, Zone>()
    private val idCounter = AtomicInteger(1)

    private fun generateNewId(): Id = Id(idCounter.getAndIncrement().toUInt())

    override suspend fun create(entity: Zone): Zone {
        val now = LocalDateTime.nowUTC
        val zoneToCreate =
            entity.copy(
                id = generateNewId(),
                photosIds = emptySet(),
                createdAt = now,
                updatedAt = now,
            )
        zonesById[zoneToCreate.id] = zoneToCreate
        return zoneToCreate
    }

    override suspend fun getById(id: Id): Zone? = zonesById[id]

    override suspend fun getAll(): List<Zone> = zonesById.values.toList()

    override suspend fun update(entity: Zone): Zone {
        val existingZone = zonesById[entity.id] ?: throw ZoneNotFoundException(entity.id)
        if (entity.photosIds != existingZone.photosIds) {
            println("Photos list was not updated. Use addPhoto/removePhoto")
        }
        val updatedZone =
            entity.copy(
                photosIds = existingZone.photosIds,
                createdAt = existingZone.createdAt,
                updatedAt = LocalDateTime.nowUTC,
            )
        zonesById[updatedZone.id] = updatedZone
        return updatedZone
    }

    override suspend fun deleteById(id: Id): Boolean = zonesById.remove(id) != null

    override suspend fun findByReporterId(reporterId: Id): List<Zone> =
        zonesById.values
            .filter { it.reporterId == reporterId }
            .toList()

    override suspend fun findByStatus(status: ZoneStatus): List<Zone> =
        zonesById.values
            .filter { it.status == status }
            .toList()

    override suspend fun findBySeverity(severity: Severity): List<Zone> =
        zonesById.values
            .filter { it.severity == severity }
            .toList()

    override suspend fun addPhoto(
        zoneId: Id,
        photoId: Id,
    ): Zone {
        val zone = zonesById[zoneId] ?: throw ZoneNotFoundException(zoneId)
        if (zone.photosIds.contains(photoId)) {
            throw PhotoAlreadyAddedException(zoneId, photoId)
        }
        val conflictingZone =
            zonesById.values.find { it.id != zoneId && it.photosIds.contains(photoId) }
        if (conflictingZone != null) {
            throw PhotoAssociatedWithDifferentZoneException(
                photoId = photoId,
                attemptedZoneId = zoneId,
                existingZoneId = conflictingZone.id,
            )
        }
        val updatedZone =
            zone.copy(photosIds = zone.photosIds + photoId, updatedAt = LocalDateTime.nowUTC)
        zonesById[zoneId] = updatedZone
        return updatedZone
    }

    override suspend fun removePhoto(
        zoneId: Id,
        photoId: Id,
    ): Zone {
        val zone = zonesById[zoneId] ?: throw ZoneNotFoundException(zoneId)
        if (!zone.photosIds.contains(photoId)) {
            throw PhotoNotAssociatedException(zoneId, photoId)
        }
        val updatedZone =
            zone.copy(photosIds = zone.photosIds - photoId, updatedAt = LocalDateTime.nowUTC)
        zonesById[zoneId] = updatedZone
        return updatedZone
    }

    override suspend fun findZones(
        center: Location?,
        radius: Double?,
        fromDate: LocalDateTime?,
        toDate: LocalDateTime?,
        statuses: List<ZoneStatus>?,
        severities: List<Severity>?,
    ): List<Zone> {
        if (center != null || radius != null) {
            println("Location filtering (center, radius) is not supported yet.")
        }

        return zonesById.values
            .filter { zone ->
                val statusMatch =
                    statuses == null || statuses.isEmpty() || statuses.contains(zone.status)
                val severityMatch =
                    severities == null || severities.isEmpty() || severities.contains(zone.severity)
                val startDateMatch = fromDate == null || zone.createdAt >= fromDate
                val endDateMatch = toDate == null || zone.createdAt <= toDate
                // val locationMatch =

                statusMatch && severityMatch && startDateMatch && endDateMatch
                // && locationMatch
            }.toList()
    }

    fun clear() {
        zonesById.clear()
        idCounter.set(1)
    }
}
