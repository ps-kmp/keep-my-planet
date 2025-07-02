package pt.isel.keepmyplanet.repository.database

import pt.isel.keepmyplanet.domain.common.Id
import pt.isel.keepmyplanet.domain.zone.Location
import pt.isel.keepmyplanet.domain.zone.Zone
import pt.isel.keepmyplanet.domain.zone.ZoneSeverity
import pt.isel.keepmyplanet.domain.zone.ZoneStatus
import pt.isel.keepmyplanet.exception.NotFoundException
import pt.isel.keepmyplanet.repository.ZoneRepository
import pt.isel.keepmyplanet.repository.database.mappers.toDomainZone
import pt.isel.keepmyplanet.utils.calculateBoundingBox
import pt.isel.keepmyplanet.utils.calculateDistanceKm
import pt.isel.keepmyplanet.utils.now
import ptiselkeepmyplanetdb.ZoneQueries
import ptiselkeepmyplanetdb.Zones

class DatabaseZoneRepository(
    private val zoneQueries: ZoneQueries,
) : ZoneRepository {
    private fun getZoneWithPhotos(dbZone: Zones): Zone {
        val photoIds =
            zoneQueries
                .getPhotoIdsForZone(dbZone.id)
                .executeAsList()
                .toSet()
        return dbZone.toDomainZone(photoIds)
    }

    private fun getZoneWithPhotos(zoneId: Id): Zone? {
        val dbZone = zoneQueries.getById(zoneId).executeAsOneOrNull() ?: return null
        return getZoneWithPhotos(dbZone)
    }

    override suspend fun create(entity: Zone): Zone {
        val currentTime = now()
        return zoneQueries.transactionWithResult {
            val insertedDbZone =
                zoneQueries
                    .insert(
                        latitude = entity.location.latitude,
                        longitude = entity.location.longitude,
                        description = entity.description,
                        reporter_id = entity.reporterId,
                        event_id = entity.eventId,
                        status = entity.status,
                        zone_severity = entity.zoneSeverity,
                        is_active = entity.isActive,
                        created_at = currentTime,
                        updated_at = currentTime,
                    ).executeAsOne()

            entity.photosIds.forEach { zoneQueries.addPhotoToZone(insertedDbZone.id, it) }
            insertedDbZone.toDomainZone(entity.photosIds)
        }
    }

    override suspend fun getById(id: Id): Zone? = getZoneWithPhotos(id)

    override suspend fun getAll(
        limit: Int,
        offset: Int,
    ): List<Zone> =
        zoneQueries
            .getAllActive(limit.toLong(), offset.toLong())
            .executeAsList()
            .map { getZoneWithPhotos(it) }

    override suspend fun update(entity: Zone): Zone {
        zoneQueries.getById(entity.id).executeAsOneOrNull()
            ?: throw NotFoundException("Zone '${entity.id}' not found.")

        return zoneQueries.transactionWithResult {
            val updatedDbZone =
                zoneQueries
                    .updateZone(
                        id = entity.id,
                        latitude = entity.location.latitude,
                        longitude = entity.location.longitude,
                        description = entity.description,
                        reporter_id = entity.reporterId,
                        event_id = entity.eventId,
                        status = entity.status,
                        zone_severity = entity.zoneSeverity,
                        is_active = entity.isActive,
                        updated_at = now(),
                    ).executeAsOne()

            zoneQueries.removeAllPhotosFromZone(entity.id)
            entity.photosIds.forEach { zoneQueries.addPhotoToZone(entity.id, it) }
            updatedDbZone.toDomainZone(entity.photosIds)
        }
    }

    override suspend fun deleteById(id: Id): Boolean {
        val deletedIdResult =
            zoneQueries.transactionWithResult {
                zoneQueries.removeAllPhotosFromZone(id)
                zoneQueries.deleteByIdReturningId(id).executeAsOneOrNull()
            }
        return deletedIdResult != null
    }

    override suspend fun findByEventId(eventId: Id): Zone? =
        zoneQueries
            .findByEventId(eventId)
            .executeAsOneOrNull()
            ?.let { getZoneWithPhotos(it) }

    override suspend fun findByReporterId(reporterId: Id): List<Zone> =
        zoneQueries
            .findByReporterId(reporterId)
            .executeAsList()
            .map { getZoneWithPhotos(it) }

    override suspend fun findBySeverity(zoneSeverity: ZoneSeverity): List<Zone> =
        zoneQueries
            .findBySeverity(zoneSeverity)
            .executeAsList()
            .map { getZoneWithPhotos(it) }

    override suspend fun findByStatus(status: ZoneStatus): List<Zone> =
        zoneQueries
            .findByStatus(status)
            .executeAsList()
            .map { getZoneWithPhotos(it) }

    override suspend fun findNearLocation(
        center: Location,
        radiusKm: Double,
        limit: Int,
        offset: Int,
    ): List<Zone> {
        val (minCoords, maxCoords) = calculateBoundingBox(center, radiusKm)
        val candidateDbZones =
            zoneQueries
                .findInBoundingBoxActive(
                    minLat = minCoords.latitude,
                    maxLat = maxCoords.latitude,
                    minLon = minCoords.longitude,
                    maxLon = maxCoords.longitude,
                ).executeAsList()

        return candidateDbZones
            .map { dbZone ->
                val zoneLocation = Location(dbZone.latitude, dbZone.longitude)
                val distance = calculateDistanceKm(zoneLocation, center)
                getZoneWithPhotos(dbZone) to distance
            }.filter { (_, distance) -> distance <= radiusKm }
            .sortedBy { it.second }
            .drop(offset)
            .take(limit)
            .map { it.first }
    }
}
