package pt.isel.keepmyplanet.repository.database

import pt.isel.keepmyplanet.domain.common.Id
import pt.isel.keepmyplanet.domain.zone.ZoneStateChange
import pt.isel.keepmyplanet.repository.ZoneStateChangeRepository
import ptiselkeepmyplanetdb.ZoneStateChangeQueries
import ptiselkeepmyplanetdb.Zone_state_changes

class DatabaseZoneStateChangeRepository(
    private val queries: ZoneStateChangeQueries,
) : ZoneStateChangeRepository {
    private fun Zone_state_changes.toDomain(): ZoneStateChange =
        ZoneStateChange(
            id = id,
            zoneId = zone_id,
            newStatus = new_status,
            changedBy = changed_by,
            triggeredByEventId = triggered_by_event_id,
            changeTime = change_time,
        )

    override suspend fun create(entity: ZoneStateChange): ZoneStateChange =
        queries.insert(
            zone_id = entity.zoneId,
            new_status = entity.newStatus,
            changed_by = entity.changedBy,
            triggered_by_event_id = entity.triggeredByEventId,
            change_time = entity.changeTime,
        ).executeAsOne().toDomain()

    override suspend fun getById(id: Id): ZoneStateChange? =
        queries.getById(id).executeAsOneOrNull()?.toDomain()

    override suspend fun findByZoneId(zoneId: Id): List<ZoneStateChange> =
        queries.findByZoneId(zoneId)
            .executeAsList()
            .map { it.toDomain() }
}
