package pt.isel.keepmyplanet.repository.mem

import pt.isel.keepmyplanet.domain.common.Description
import pt.isel.keepmyplanet.domain.common.Id
import pt.isel.keepmyplanet.domain.common.Location
import pt.isel.keepmyplanet.domain.zone.Zone
import pt.isel.keepmyplanet.domain.zone.ZoneSeverity
import pt.isel.keepmyplanet.domain.zone.ZoneStatus
import pt.isel.keepmyplanet.errors.NotFoundException
import pt.isel.keepmyplanet.repository.ZoneRepository
import pt.isel.keepmyplanet.util.calculateDistanceKm
import pt.isel.keepmyplanet.util.now
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.atomic.AtomicInteger

class InMemoryZoneRepository : ZoneRepository {
    private val zones = ConcurrentHashMap<Id, Zone>()
    private val nextId = AtomicInteger(1)

    init {
        val zone1 =
            Zone(
                id = Id(1U),
                location = Location(latitude = 38.736946, longitude = -9.142685),
                description = Description("Zona de limpeza no Parque das Nações"),
                reporterId = Id(1U),
                eventId = null,
                status = ZoneStatus.REPORTED,
                zoneSeverity = ZoneSeverity.LOW,
                photosIds = emptySet(),
                createdAt = now(),
                updatedAt = now(),
            )

        val zone2 =
            Zone(
                id = Id(2U),
                location = Location(latitude = 41.157944, longitude = -8.629105),
                description = Description("Zona de reflorestação no Parque da Cidade"),
                reporterId = Id(2U),
                eventId = null,
                status = ZoneStatus.REPORTED,
                zoneSeverity = ZoneSeverity.MEDIUM,
                photosIds = emptySet(),
                createdAt = now(),
                updatedAt = now(),
            )

        zones[zone1.id] = zone1
        zones[zone2.id] = zone2
    }

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
        val existingZone =
            zones[entity.id] ?: throw NotFoundException("Zone '${entity.id}' not found.")
        val updatedZone = entity.copy(createdAt = existingZone.createdAt, updatedAt = now())
        zones[entity.id] = updatedZone
        return updatedZone
    }

    override suspend fun deleteById(id: Id): Boolean = zones.remove(id) != null

    override suspend fun findByEventId(eventId: Id): Zone? =
        zones.values
            .find { it.id == eventId }

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
