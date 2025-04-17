package pt.isel.keepmyplanet.repository

import pt.isel.keepmyplanet.domain.common.Id
import pt.isel.keepmyplanet.domain.message.Message

interface MessageRepository : Repository<Message, Id> {
    suspend fun getAllByEventId(eventId: Id): List<Message>

    suspend fun getAllBySenderId(senderId: Id): List<Message>

    suspend fun getSingleByEventIdAndSeqNum(
        eventId: Id,
        sequenceNum: Int,
    ): Message?

    suspend fun deleteAllByEventId(eventId: Id): Int
}
