package pt.isel.keepmyplanet.repository

import kotlinx.coroutines.flow.Flow
import pt.isel.keepmyplanet.domain.common.Id
import pt.isel.keepmyplanet.domain.message.Message

interface MessageRepository : Repository<Message, Id> {
    fun findByEventId(eventId: Id): Flow<List<Message>>

    fun findBySenderId(senderId: Id): Flow<List<Message>>

    suspend fun deleteAllByEventId(eventId: Id): Int
}
