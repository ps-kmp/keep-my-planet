package pt.isel.keepmyplanet.domain.event

import kotlinx.datetime.LocalDateTime
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

class PeriodTest {
    private val now = LocalDateTime(2000, 1, 1, 1, 1)
    private val future = LocalDateTime(2000, 1, 1, 1, 2)

    @Test
    fun `valid Period should be created when start is before end`() {
        Period(start = now, end = future)
    }

    @Test
    fun `Period should throw when start equals end`() {
        assertFailsWith<IllegalArgumentException> { Period(start = now, end = now) }
    }

    @Test
    fun `Period should throw when start is after end`() {
        assertFailsWith<IllegalArgumentException> { Period(start = future, end = now) }
    }

    @Test
    fun `properties should return correct values`() {
        val period = Period(now, future)
        assertEquals(now, period.start)
        assertEquals(future, period.end)
    }
}
