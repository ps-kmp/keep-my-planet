package pt.isel.keepmyplanet.domain.message

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

class MessageContentTest {
    @Test
    fun `valid MessageContent should be created`() {
        MessageContent("Hello")
        MessageContent("H")
        val maxLengthContent = "a".repeat(1000)
        MessageContent(maxLengthContent)
    }

    @Test
    fun `blank MessageContent should throw exception`() {
        assertFailsWith<IllegalArgumentException> { MessageContent("") }
        assertFailsWith<IllegalArgumentException> { MessageContent("   ") }
    }

    @Test
    fun `too long MessageContent should throw exception`() {
        val tooLongContent = "a".repeat(1001)
        assertFailsWith<IllegalArgumentException> { MessageContent(tooLongContent) }
    }

    @Test
    fun `value property should return correct string`() {
        val text = "Test message content"
        val content = MessageContent(text)
        assertEquals(text, content.value)
    }
}
