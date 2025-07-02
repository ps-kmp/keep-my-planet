package pt.isel.keepmyplanet.repository.memory

import java.util.concurrent.ConcurrentHashMap
import kotlin.collections.set
import pt.isel.keepmyplanet.domain.common.Id
import pt.isel.keepmyplanet.domain.zone.ZoneStateChange
import pt.isel.keepmyplanet.repository.ZoneStateChangeRepository

class InMemoryZoneStateChangeRepository : ZoneStateChangeRepository {
    private val data = ConcurrentHashMap<Id, ZoneStateChange>()

    override suspend fun create(entity: ZoneStateChange): ZoneStateChange {
        data[entity.id] = entity
        return entity
    }

    override suspend fun getById(id: Id): ZoneStateChange? = data[id]

    override suspend fun findByZoneId(zoneId: Id): List<ZoneStateChange> =
        data.values.filter { it.zoneId == zoneId }
}
