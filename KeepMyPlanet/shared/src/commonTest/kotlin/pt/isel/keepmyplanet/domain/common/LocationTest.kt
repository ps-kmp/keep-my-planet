package pt.isel.keepmyplanet.domain.common

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

class LocationTest {

    @Test
    fun `valid Location should be created`() {
        Location(-90.0, -180.0)
        Location(90.0, 180.0)
        Location(0.0, 0.0)
        Location(38.7569, -9.1165)
    }

    @Test
    fun `invalid latitude should throw exception`() {
        assertFailsWith<IllegalArgumentException> { Location(-90.1, 0.0) }
        assertFailsWith<IllegalArgumentException> { Location(90.1, 0.0) }
    }

    @Test
    fun `invalid longitude should throw exception`() {
        assertFailsWith<IllegalArgumentException> { Location(0.0, -180.1) }
        assertFailsWith<IllegalArgumentException> { Location(0.0, 180.1) }
    }

    @Test
    fun `properties should return correct values`() {
        val lat = 38.4349
        val long = 9.9162
        val location = Location(lat, long)
        assertEquals(lat, location.latitude)
        assertEquals(long, location.longitude)
    }
}
