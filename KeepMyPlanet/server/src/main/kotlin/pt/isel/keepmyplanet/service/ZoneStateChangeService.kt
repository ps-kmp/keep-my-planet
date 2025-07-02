package pt.isel.keepmyplanet.service

import pt.isel.keepmyplanet.domain.common.Id
import pt.isel.keepmyplanet.domain.zone.Zone
import pt.isel.keepmyplanet.domain.zone.ZoneStateChange
import pt.isel.keepmyplanet.domain.zone.ZoneStatus
import pt.isel.keepmyplanet.repository.ZoneRepository
import pt.isel.keepmyplanet.repository.ZoneStateChangeRepository

class ZoneStateChangeService(
    private val zoneRepository: ZoneRepository,
    private val zoneStateChangeRepository: ZoneStateChangeRepository,
) {

    suspend fun changeZoneStatus(
        zone: Zone,
        newStatus: ZoneStatus,
        changedBy: Id?,
        triggeredByEventId: Id? = null,
    ): Zone {
        if (zone.status == newStatus) return zone

        val updatedZone = zone.copy(status = newStatus)
        val finalZone = zoneRepository.update(updatedZone)

        val logEntry = ZoneStateChange(
            id = Id(0u),
            zoneId = finalZone.id,
            newStatus = newStatus,
            changedBy = changedBy,
            triggeredByEventId = triggeredByEventId,
            changeTime = finalZone.updatedAt
        )
        zoneStateChangeRepository.create(logEntry)

        return finalZone
    }

    suspend fun archiveZone(zone: Zone): Zone {
        if (!zone.isActive) return zone

        val updatedZone = zone.copy(isActive = false)
        return zoneRepository.update(updatedZone)
    }
}
