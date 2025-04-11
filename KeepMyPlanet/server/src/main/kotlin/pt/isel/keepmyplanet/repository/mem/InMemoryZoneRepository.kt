package pt.isel.keepmyplanet.repository.mem

import pt.isel.keepmyplanet.core.NotFoundException
import pt.isel.keepmyplanet.domain.common.Id
import pt.isel.keepmyplanet.domain.common.Location
import pt.isel.keepmyplanet.domain.zone.Zone
import pt.isel.keepmyplanet.domain.zone.ZoneSeverity
import pt.isel.keepmyplanet.domain.zone.ZoneStatus
import pt.isel.keepmyplanet.repository.ZoneRepository
import pt.isel.keepmyplanet.util.calculateDistanceKm
import pt.isel.keepmyplanet.util.now
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.atomic.AtomicInteger

class InMemoryZoneRepository : ZoneRepository {
    private val zones = ConcurrentHashMap<Id, Zone>()
    private val nextId = AtomicInteger(1)

    override suspend fun create(entity: Zone): Zone {
        val newId = Id(nextId.getAndIncrement().toUInt())
        val currentTime = now()
        val newZone = entity.copy(id = newId, createdAt = currentTime, updatedAt = currentTime)
        zones[newId] = newZone
        return newZone
    }

    override suspend fun getById(id: Id): Zone? = zones[id]

    override suspend fun getAll(): List<Zone> =
        zones.values
            .toList()
            .sortedBy { it.id.value }

    override suspend fun update(entity: Zone): Zone {
        val existingZone = zones[entity.id] ?: throw NotFoundException("Zone", entity.id)
        val updatedZone = entity.copy(createdAt = existingZone.createdAt, updatedAt = now())
        zones[entity.id] = updatedZone
        return updatedZone
    }

    override suspend fun deleteById(id: Id): Boolean = zones.remove(id) != null

    override suspend fun findByEventId(eventId: Id): Zone? = zones[eventId]

    override suspend fun findByReporterId(reporterId: Id): List<Zone> =
        zones.values
            .filter { it.reporterId == reporterId }
            .sortedBy { it.id.value }

    override suspend fun findBySeverity(zoneSeverity: ZoneSeverity): List<Zone> =
        zones.values
            .filter { it.zoneSeverity == zoneSeverity }
            .sortedBy { it.id.value }

    override suspend fun findByStatus(status: ZoneStatus): List<Zone> =
        zones.values
            .filter { it.status == status }
            .sortedBy { it.id.value }

    override suspend fun findNearLocation(
        center: Location,
        radiusKm: Double,
    ): List<Zone> =
        zones.values
            .filter { calculateDistanceKm(it.location, center) <= radiusKm }
            .sortedBy { it.id.value }

    fun clear() {
        zones.clear()
        nextId.set(1)
    }
}
