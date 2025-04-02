package pt.isel.keepmyplanet.repository

import kotlinx.coroutines.flow.Flow
import kotlinx.datetime.LocalDateTime
import pt.isel.keepmyplanet.domain.common.Id
import pt.isel.keepmyplanet.domain.common.Location
import pt.isel.keepmyplanet.domain.zone.Severity
import pt.isel.keepmyplanet.domain.zone.Zone
import pt.isel.keepmyplanet.domain.zone.ZoneStatus

interface ZoneRepository : Repository<Zone, Id> {
    fun findByReporterId(reporterId: Id): Flow<List<Zone>>

    fun findByStatus(status: ZoneStatus): Flow<List<Zone>>

    fun findBySeverity(severity: Severity): Flow<List<Zone>>

    suspend fun addPhoto(zoneId: Id, photoId: Id): Zone

    suspend fun removePhoto(zoneId: Id, photoId: Id): Zone

    fun findZones(
        center: Location? = null,
        radius: Double? = null,
        fromDate: LocalDateTime? = null,
        toDate: LocalDateTime? = null,
        statuses: List<ZoneStatus>? = null,
        severities: List<Severity>? = null,
    ): Flow<List<Zone>>
}
