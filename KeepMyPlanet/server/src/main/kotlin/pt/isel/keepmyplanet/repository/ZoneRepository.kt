package pt.isel.keepmyplanet.repository

import pt.isel.keepmyplanet.domain.common.Id
import pt.isel.keepmyplanet.domain.common.Location
import pt.isel.keepmyplanet.domain.zone.Zone
import pt.isel.keepmyplanet.domain.zone.ZoneSeverity
import pt.isel.keepmyplanet.domain.zone.ZoneStatus

interface ZoneRepository : Repository<Zone, Id> {
    suspend fun findByEventId(eventId: Id): Zone?

    suspend fun findByReporterId(reporterId: Id): List<Zone>

    suspend fun findBySeverity(zoneSeverity: ZoneSeverity): List<Zone>

    suspend fun findByStatus(status: ZoneStatus): List<Zone>

    suspend fun findNearLocation(
        center: Location,
        radiusKm: Double,
        limit: Int = 20,
        offset: Int = 0,
    ): List<Zone>
}
