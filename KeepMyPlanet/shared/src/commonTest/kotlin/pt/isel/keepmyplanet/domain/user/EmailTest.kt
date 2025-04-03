package pt.isel.keepmyplanet.domain.user

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

class EmailTest {
    @Test
    fun `valid emails should be created`() {
        Email("user@example.com")
        Email("first.last@example.co.uk")
        Email("user+tag@example.org")
        Email("user123@sub.domain.com")
    }

    @Test
    fun `blank email should throw exception`() {
        assertFailsWith<IllegalArgumentException> { Email("") }
        assertFailsWith<IllegalArgumentException> { Email("   ") }
    }

    @Test
    fun `invalid email formats should throw exception`() {
        assertFailsWith<IllegalArgumentException> { Email("plainstring") }
        assertFailsWith<IllegalArgumentException> { Email("user@.com") }
        assertFailsWith<IllegalArgumentException> { Email("@example.com") }
        assertFailsWith<IllegalArgumentException> { Email("user@domain..com") }
    }

    @Test
    fun `value property should return correct string`() {
        val emailStr = "test@example.com"
        val email = Email(emailStr)
        assertEquals(emailStr, email.value)
    }
}
