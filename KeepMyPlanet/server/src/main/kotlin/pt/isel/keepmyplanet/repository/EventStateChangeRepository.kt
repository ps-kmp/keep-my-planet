package pt.isel.keepmyplanet.repository

import pt.isel.keepmyplanet.domain.common.Id
import pt.isel.keepmyplanet.domain.event.EventStateChange
import pt.isel.keepmyplanet.domain.event.EventStateChangeDetails

interface EventStateChangeRepository : Repository<EventStateChange, Id> {
    suspend fun findByEventId(eventId: Id): List<EventStateChange>

    suspend fun findByEventIdWithDetails(eventId: Id): List<EventStateChangeDetails>

    suspend fun save(eventStateChange: EventStateChange): EventStateChange
}
