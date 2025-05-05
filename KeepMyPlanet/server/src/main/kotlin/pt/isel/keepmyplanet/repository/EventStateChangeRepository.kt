package pt.isel.keepmyplanet.repository

import pt.isel.keepmyplanet.domain.common.Id
import pt.isel.keepmyplanet.domain.event.EventStateChange

interface EventStateChangeRepository : Repository<EventStateChange, Id> {
    suspend fun findByEventId(eventId: Id): List<EventStateChange>

    suspend fun save(eventStateChange: EventStateChange): EventStateChange
}
