package pt.isel.keepmyplanet.domain.zone

import kotlin.test.Test
import kotlin.test.assertEquals

class SeverityTest {
    @Test
    fun `should contain exactly three enum values in correct order`() {
        val values = Severity.entries.toTypedArray()
        assertEquals(4, values.size)
        assertEquals(Severity.UNKNOWN, values[0])
        assertEquals(Severity.LOW, values[1])
        assertEquals(Severity.MEDIUM, values[2])
        assertEquals(Severity.HIGH, values[3])
    }

    @Test
    fun `should convert from string name`() {
        assertEquals(Severity.UNKNOWN, Severity.valueOf("UNKNOWN"))
        assertEquals(Severity.LOW, Severity.valueOf("LOW"))
        assertEquals(Severity.MEDIUM, Severity.valueOf("MEDIUM"))
        assertEquals(Severity.HIGH, Severity.valueOf("HIGH"))
    }
}
