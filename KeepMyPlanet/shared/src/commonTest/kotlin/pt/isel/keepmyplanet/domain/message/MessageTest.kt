package pt.isel.keepmyplanet.domain.message

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlinx.datetime.LocalDateTime
import pt.isel.keepmyplanet.domain.common.Id
import pt.isel.keepmyplanet.domain.user.Name

class MessageTest {
    private val validId = Id(1u)
    private val validEventId = Id(2u)
    private val validSenderId = Id(3u)
    private val validSenderName = Name("user")
    private val validContent = MessageContent("Hello everyone!")
    private val validTimestamp = LocalDateTime(2000, 1, 1, 1, 1)
    private val chatPosition = 1

    @Test
    fun `valid Message should be created with all required fields`() {
        Message(
            id = validId,
            eventId = validEventId,
            senderId = validSenderId,
            senderName = validSenderName,
            content = validContent,
            timestamp = validTimestamp,
            chatPosition = chatPosition,
        )
    }

    @Test
    fun `properties should return correct values`() {
        val message =
            Message(
                id = validId,
                eventId = validEventId,
                senderId = validSenderId,
                senderName = validSenderName,
                content = validContent,
                timestamp = validTimestamp,
                chatPosition = chatPosition,
            )
        assertEquals(validId, message.id)
        assertEquals(validEventId, message.eventId)
        assertEquals(validSenderId, message.senderId)
        assertEquals(validContent, message.content)
        assertEquals(validTimestamp, message.timestamp)
        assertEquals(chatPosition, message.chatPosition)
    }
}
