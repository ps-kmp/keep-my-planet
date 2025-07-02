package pt.isel.keepmyplanet.repository

import pt.isel.keepmyplanet.domain.common.Id
import pt.isel.keepmyplanet.domain.zone.ZoneStateChange

interface ZoneStateChangeRepository {
    suspend fun create(entity: ZoneStateChange): ZoneStateChange

    suspend fun findByZoneId(zoneId: Id): List<ZoneStateChange>

    suspend fun getById(id: Id): ZoneStateChange?
}
