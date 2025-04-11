package pt.isel.keepmyplanet.domain.zone

import kotlin.test.Test
import kotlin.test.assertEquals

class ZoneSeverityTest {
    @Test
    fun `should contain exactly three enum values in correct order`() {
        val values = ZoneSeverity.entries.toTypedArray()
        assertEquals(4, values.size)
        assertEquals(ZoneSeverity.UNKNOWN, values[0])
        assertEquals(ZoneSeverity.LOW, values[1])
        assertEquals(ZoneSeverity.MEDIUM, values[2])
        assertEquals(ZoneSeverity.HIGH, values[3])
    }

    @Test
    fun `should convert from string name`() {
        assertEquals(ZoneSeverity.UNKNOWN, ZoneSeverity.valueOf("UNKNOWN"))
        assertEquals(ZoneSeverity.LOW, ZoneSeverity.valueOf("LOW"))
        assertEquals(ZoneSeverity.MEDIUM, ZoneSeverity.valueOf("MEDIUM"))
        assertEquals(ZoneSeverity.HIGH, ZoneSeverity.valueOf("HIGH"))
    }
}
