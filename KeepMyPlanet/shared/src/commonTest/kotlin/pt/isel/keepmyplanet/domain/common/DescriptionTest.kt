package pt.isel.keepmyplanet.domain.common

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

class DescriptionTest {

    @Test
    fun `valid Description should be created`() {
        Description("Valid description")
        val maxLengthText = "a".repeat(1000)
        Description(maxLengthText)
    }

    @Test
    fun `blank Description should throw exception`() {
        assertFailsWith<IllegalArgumentException> { Description("") }
        assertFailsWith<IllegalArgumentException> { Description("   ") }
    }

    @Test
    fun `too long Description should throw exception`() {
        val tooLongText = "a".repeat(1001)
        assertFailsWith<IllegalArgumentException> { Description(tooLongText) }
    }

    @Test
    fun `value property should return correct string`() {
        val text = "Test description"
        val description = Description(text)
        assertEquals(text, description.value)
    }
}
