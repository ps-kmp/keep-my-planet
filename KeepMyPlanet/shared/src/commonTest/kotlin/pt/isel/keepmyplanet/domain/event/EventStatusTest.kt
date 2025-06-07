package pt.isel.keepmyplanet.domain.event

import kotlin.test.Test
import kotlin.test.assertEquals

class EventStatusTest {
    @Test
    fun `should have correct enum values in order`() {
        val values = EventStatus.entries.toTypedArray()
        assertEquals(5, values.size)
        assertEquals(EventStatus.PLANNED, values[0])
        assertEquals(EventStatus.IN_PROGRESS, values[1])
        assertEquals(EventStatus.COMPLETED, values[2])
        assertEquals(EventStatus.CANCELLED, values[3])
        assertEquals(EventStatus.UNKNOWN, values[4])
    }

    @Test
    fun `should convert from string`() {
        assertEquals(EventStatus.PLANNED, EventStatus.valueOf("PLANNED"))
        assertEquals(EventStatus.IN_PROGRESS, EventStatus.valueOf("IN_PROGRESS"))
        assertEquals(EventStatus.COMPLETED, EventStatus.valueOf("COMPLETED"))
        assertEquals(EventStatus.CANCELLED, EventStatus.valueOf("CANCELLED"))
    }
}
