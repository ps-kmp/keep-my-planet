package pt.isel.keepmyplanet.domain.user

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

class NameTest {
    @Test
    fun `valid names should be created`() {
        Name("John Doe")
        Name("J")
        val maxLengthName = "A".repeat(30)
        Name(maxLengthName)
    }

    @Test
    fun `blank names should throw exception`() {
        assertFailsWith<IllegalArgumentException> { Name("") }
        assertFailsWith<IllegalArgumentException> { Name("   ") }
    }

    @Test
    fun `names exceeding 30 characters should throw exception`() {
        val tooLongName = "A".repeat(31)
        assertFailsWith<IllegalArgumentException> { Name(tooLongName) }
    }

    @Test
    fun `value property should return correct string`() {
        val nameStr = "Alice"
        val name = Name(nameStr)
        assertEquals(nameStr, name.value)
    }
}
