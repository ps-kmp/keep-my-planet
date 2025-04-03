package pt.isel.keepmyplanet.repository

import kotlinx.datetime.LocalDateTime
import pt.isel.keepmyplanet.domain.common.Id
import pt.isel.keepmyplanet.domain.common.Location
import pt.isel.keepmyplanet.domain.zone.Severity
import pt.isel.keepmyplanet.domain.zone.Zone
import pt.isel.keepmyplanet.domain.zone.ZoneStatus

interface ZoneRepository : Repository<Zone, Id> {
    suspend fun findByReporterId(reporterId: Id): List<Zone>

    suspend fun findByStatus(status: ZoneStatus): List<Zone>

    suspend fun findBySeverity(severity: Severity): List<Zone>

    suspend fun addPhoto(
        zoneId: Id,
        photoId: Id,
    ): Zone

    suspend fun removePhoto(
        zoneId: Id,
        photoId: Id,
    ): Zone

    suspend fun findZones(
        center: Location? = null,
        radius: Double? = null,
        fromDate: LocalDateTime? = null,
        toDate: LocalDateTime? = null,
        statuses: List<ZoneStatus>? = null,
        severities: List<Severity>? = null,
    ): List<Zone>
}
