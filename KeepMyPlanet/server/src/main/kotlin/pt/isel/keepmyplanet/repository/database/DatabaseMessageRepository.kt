package pt.isel.keepmyplanet.repository.database

import pt.isel.keepmyplanet.domain.common.Id
import pt.isel.keepmyplanet.domain.message.Message
import pt.isel.keepmyplanet.exception.NotFoundException
import pt.isel.keepmyplanet.repository.MessageRepository
import pt.isel.keepmyplanet.repository.database.mappers.toDomainMessage
import pt.isel.keepmyplanet.utils.now
import ptiselkeepmyplanetdb.MessageQueries

class DatabaseMessageRepository(
    private val messageQueries: MessageQueries,
) : MessageRepository {
    override suspend fun create(entity: Message): Message =
        messageQueries.transactionWithResult {
            val maxPos =
                messageQueries
                    .getMaxChatPositionForEvent(entity.eventId)
                    .executeAsOneOrNull()
            val nextChatPosition = ((maxPos?.MAX ?: -1) + 1)

            val currentTime = now()
            messageQueries
                .insert(
                    event_id = entity.eventId,
                    sender_id = entity.senderId,
                    sender_name = entity.senderName,
                    content = entity.content,
                    timestamp = currentTime,
                    chat_position = nextChatPosition,
                ).executeAsOne()
                .toDomainMessage()
        }

    override suspend fun getById(id: Id): Message? =
        messageQueries
            .getById(id)
            .executeAsOneOrNull()
            ?.toDomainMessage()

    override suspend fun getAllByEventId(eventId: Id): List<Message> =
        messageQueries
            .getAllByEventId(eventId)
            .executeAsList()
            .map { it.toDomainMessage() }

    override suspend fun getSingleByEventIdAndSeqNum(
        eventId: Id,
        sequenceNum: Int,
    ): Message? =
        messageQueries
            .getSingleByEventIdAndSeqNum(eventId, sequenceNum)
            .executeAsOneOrNull()
            ?.toDomainMessage()

    override suspend fun deleteById(id: Id): Boolean =
        messageQueries
            .deleteByIdReturningId(id)
            .executeAsOneOrNull() != null

    override suspend fun getAll(
        limit: Int,
        offset: Int,
    ): List<Message> =
        messageQueries
            .getAll(limit.toLong(), offset.toLong())
            .executeAsList()
            .map { it.toDomainMessage() }

    override suspend fun update(entity: Message): Message {
        messageQueries.getById(entity.id).executeAsOneOrNull()
            ?: throw NotFoundException("Message '${entity.id}' not found.")

        return messageQueries.transactionWithResult {
            messageQueries
                .updateMessage(
                    id = entity.id,
                    content = entity.content,
                    timestamp = now(),
                ).executeAsOne()
                .toDomainMessage()
        }
    }

    override suspend fun getAllBySenderId(senderId: Id): List<Message> =
        messageQueries
            .getAllBySenderId(senderId)
            .executeAsList()
            .map { it.toDomainMessage() }

    override suspend fun deleteAllByEventId(eventId: Id): Int =
        messageQueries
            .deleteAllByEventIdReturningIds(eventId)
            .executeAsList()
            .size
}
