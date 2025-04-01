package pt.isel.keepmyplanet.domain.common

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

class TitleTest {

    @Test
    fun `valid Title should be created`() {
        Title("Valid Title")
        Title("A")
        val maxLengthTitle = "a".repeat(150)
        Title(maxLengthTitle)
    }

    @Test
    fun `blank Title should throw exception`() {
        assertFailsWith<IllegalArgumentException> { Title("") }
        assertFailsWith<IllegalArgumentException> { Title("   ") }
    }

    @Test
    fun `too long Title should throw exception`() {
        val tooLongTitle = "a".repeat(151)
        assertFailsWith<IllegalArgumentException> { Title(tooLongTitle) }
    }

    @Test
    fun `value property should return correct string`() {
        val text = "Test Title"
        val title = Title(text)
        assertEquals(text, title.value)
    }
}
