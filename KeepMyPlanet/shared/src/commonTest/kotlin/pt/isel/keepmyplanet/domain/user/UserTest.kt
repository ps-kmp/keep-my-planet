package pt.isel.keepmyplanet.domain.user

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlinx.datetime.LocalDateTime
import pt.isel.keepmyplanet.domain.common.Id

class UserTest {
    private val validId = Id(1u)
    private val validName = Name("John Doe")
    private val validEmail = Email("john@example.com")
    private val validPasswordHash = PasswordHash("Password1!")
    private val validDateTime = LocalDateTime(2000, 1, 1, 1, 1)

    @Test
    fun `valid User should be created with all required fields`() {
        User(
            id = validId,
            name = validName,
            email = validEmail,
            passwordHash = validPasswordHash,
            createdAt = validDateTime,
            updatedAt = validDateTime,
        )
    }

    @Test
    fun `User with optional profilePictureId should be created`() {
        val user =
            User(
                id = validId,
                name = validName,
                email = validEmail,
                passwordHash = validPasswordHash,
                profilePictureId = Id(2u),
                createdAt = validDateTime,
                updatedAt = validDateTime,
            )
        assertEquals(Id(2u), user.profilePictureId)
    }

    @Test
    fun `User without profilePictureId should have null value`() {
        val user =
            User(
                id = validId,
                name = validName,
                email = validEmail,
                passwordHash = validPasswordHash,
                createdAt = validDateTime,
                updatedAt = validDateTime,
            )
        assertNull(user.profilePictureId)
    }

    @Test
    fun `properties should return correct values`() {
        val user =
            User(
                id = validId,
                name = validName,
                email = validEmail,
                passwordHash = validPasswordHash,
                profilePictureId = Id(3u),
                createdAt = validDateTime,
                updatedAt = validDateTime,
            )
        assertEquals(validId, user.id)
        assertEquals(validName, user.name)
        assertEquals(validEmail, user.email)
        assertEquals(validPasswordHash, user.passwordHash)
        assertEquals(Id(3u), user.profilePictureId)
        assertEquals(validDateTime, user.createdAt)
        assertEquals(validDateTime, user.updatedAt)
    }
}
