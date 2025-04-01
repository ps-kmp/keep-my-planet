package pt.isel.keepmyplanet.domain.zone

import kotlin.test.Test
import kotlin.test.assertEquals

class ZoneStatusTest {

    @Test
    fun `should contain exactly three expected enum values`() {
        val values = ZoneStatus.entries.toTypedArray()
        assertEquals(3, values.size)
        assertEquals(ZoneStatus.REPORTED, values[0])
        assertEquals(ZoneStatus.CLEANING_SCHEDULED, values[1])
        assertEquals(ZoneStatus.CLEANED, values[2])
    }

    @Test
    fun `should convert from string name successfully`() {
        assertEquals(ZoneStatus.REPORTED, ZoneStatus.valueOf("REPORTED"))
        assertEquals(ZoneStatus.CLEANING_SCHEDULED, ZoneStatus.valueOf("CLEANING_SCHEDULED"))
        assertEquals(ZoneStatus.CLEANED, ZoneStatus.valueOf("CLEANED"))
    }
}
