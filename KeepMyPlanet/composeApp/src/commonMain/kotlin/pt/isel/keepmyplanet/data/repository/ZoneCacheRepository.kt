package pt.isel.keepmyplanet.data.repository

import kotlinx.datetime.Clock
import kotlinx.datetime.LocalDateTime
import kotlinx.serialization.json.Json
import pt.isel.keepmyplanet.cache.KeepMyPlanetCache
import pt.isel.keepmyplanet.cache.ZoneCache
import pt.isel.keepmyplanet.domain.common.Description
import pt.isel.keepmyplanet.domain.common.Id
import pt.isel.keepmyplanet.domain.zone.Location
import pt.isel.keepmyplanet.domain.zone.Zone
import pt.isel.keepmyplanet.domain.zone.ZoneSeverity
import pt.isel.keepmyplanet.domain.zone.ZoneStatus
import pt.isel.keepmyplanet.utils.safeValueOf

fun ZoneCache.toZone(photoIds: Set<Id>): Zone =
    Zone(
        id = Id(this.id.toUInt()),
        location = Location(this.latitude, this.longitude),
        description = Description(this.description),
        reporterId = Id(this.reporterId.toUInt()),
        eventId = this.eventId?.let { Id(it.toUInt()) },
        status = safeValueOf<ZoneStatus>(this.status) ?: ZoneStatus.REPORTED,
        zoneSeverity = safeValueOf<ZoneSeverity>(this.zoneSeverity) ?: ZoneSeverity.UNKNOWN,
        photosIds = photoIds,
        createdAt = LocalDateTime.parse(this.createdAt),
        updatedAt = LocalDateTime.parse(this.updatedAt),
    )

class ZoneCacheRepository(
    database: KeepMyPlanetCache,
) {
    private val queries = database.zoneCacheQueries

    suspend fun insertZones(zones: List<Zone>) {
        queries.transaction {
            zones.forEach { zone ->
                val photosJson = Json.encodeToString(zone.photosIds.map { it.value })
                queries.insertZone(
                    id = zone.id.value.toLong(),
                    latitude = zone.location.latitude,
                    longitude = zone.location.longitude,
                    description = zone.description.value,
                    reporterId = zone.reporterId.value.toLong(),
                    eventId = zone.eventId?.value?.toLong(),
                    status = zone.status.name,
                    zoneSeverity = zone.zoneSeverity.name,
                    photosIds_json = photosJson,
                    createdAt = zone.createdAt.toString(),
                    updatedAt = zone.updatedAt.toString(),
                    timestamp = Clock.System.now().epochSeconds,
                )
            }
        }
    }

    fun getZonesInBoundingBox(
        min: Location,
        max: Location,
    ): List<Zone> =
        queries
            .getZonesInBoundingBox(
                minLat = min.latitude,
                maxLat = max.latitude,
                minLon = min.longitude,
                maxLon = max.longitude,
            ).executeAsList()
            .map { dbZone ->
                val photoIds =
                    Json
                        .decodeFromString<Set<UInt>>(dbZone.photosIds_json)
                        .map { Id(it) }
                        .toSet()
                dbZone.toZone(photoIds)
            }

    suspend fun clearAllZones() {
        queries.clearAllZones()
    }

    fun getZoneById(id: Id): Zone? {
        val dbZone = queries.getById(id.value.toLong()).executeAsOneOrNull() ?: return null
        val photoIds =
            Json.decodeFromString<Set<UInt>>(dbZone.photosIds_json).map { Id(it) }.toSet()
        return dbZone.toZone(photoIds)
    }

    suspend fun deleteExpiredZones(ttlSeconds: Long) {
        val expirationTime = Clock.System.now().epochSeconds - ttlSeconds
        queries.deleteExpiredZones(expirationTime)
    }
}
