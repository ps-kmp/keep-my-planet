package pt.isel.keepmyplanet.repository

import pt.isel.keepmyplanet.domain.common.Id
import pt.isel.keepmyplanet.domain.message.Message

interface MessageRepository : Repository<Message, Id> {
    fun findByEventId(eventId: Id): List<Message>

    fun findBySenderId(senderId: Id): List<Message>

    suspend fun deleteAllByEventId(eventId: Id): Int
}
