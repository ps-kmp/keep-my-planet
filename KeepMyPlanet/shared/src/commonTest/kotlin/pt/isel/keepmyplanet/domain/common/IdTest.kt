package pt.isel.keepmyplanet.domain.common

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

class IdTest {
    @Test
    fun `valid Id should be created`() {
        Id(1u)
        Id(42u)
        Id(UInt.MAX_VALUE)
    }

    @Test
    fun `zero Id should throw exception`() {
        assertFailsWith<IllegalArgumentException> { Id(0u) }
    }

    @Test
    fun `value property should return correct UInt`() {
        val number = 42u
        val id = Id(number)
        assertEquals(number, id.value)
    }
}
