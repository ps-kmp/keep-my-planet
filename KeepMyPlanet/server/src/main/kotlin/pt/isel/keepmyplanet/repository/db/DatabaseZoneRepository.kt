package pt.isel.keepmyplanet.repository.db

import pt.isel.keepmyplanet.domain.common.Id
import pt.isel.keepmyplanet.domain.common.Location
import pt.isel.keepmyplanet.domain.zone.Zone
import pt.isel.keepmyplanet.domain.zone.ZoneSeverity
import pt.isel.keepmyplanet.domain.zone.ZoneStatus
import pt.isel.keepmyplanet.errors.NotFoundException
import pt.isel.keepmyplanet.repository.ZoneRepository
import pt.isel.keepmyplanet.util.calculateBoundingBox
import pt.isel.keepmyplanet.util.calculateDistanceKm
import pt.isel.keepmyplanet.util.now
import ptiselkeepmyplanetdb.ZoneQueries
import ptiselkeepmyplanetdb.Zones

private fun Zones.toDomainZone(photoIds: Set<Id>): Zone =
    Zone(
        id = this.id,
        location = Location(latitude = this.latitude, longitude = this.longitude),
        description = this.description,
        reporterId = this.reporter_id,
        eventId = this.event_id,
        status = this.status,
        zoneSeverity = this.zone_severity,
        photosIds = photoIds,
        createdAt = this.created_at,
        updatedAt = this.updated_at,
    )

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
            .getAll(limit.toLong(), offset.toLong())
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
                .findInBoundingBox(
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
