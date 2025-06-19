package pt.isel.keepmyplanet.domain.common

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlinx.datetime.LocalDateTime

class PhotoTest {
    private val validId = Id(1u)
    private val validHttpUrl = Url("http://example.com")
    private val validHttpsUrl = Url("https://example.com")
    private val validDescription = Description("A valid description")
    private val validDateTime = LocalDateTime(2000, 1, 1, 1, 1)

    @Test
    fun `valid Photo should be created with HTTP or HTTPS URL`() {
        Photo(id = validId, url = validHttpUrl, uploaderId = validId, uploadedAt = validDateTime)
        Photo(
            id = validId,
            url = validHttpsUrl,
            description = validDescription,
            uploaderId = validId,
            uploadedAt = validDateTime,
        )
    }

    @Test
    fun `Photo with non-HTTP URL should throw exception`() {
        assertFailsWith<IllegalArgumentException> {
            Photo(
                id = validId,
                url = Url("ftp://example.com"),
                uploaderId = validId,
                uploadedAt = validDateTime,
            )
        }
    }

    @Test
    fun `Photo properties should return correct values`() {
        val photo =
            Photo(
                id = validId,
                url = validHttpsUrl,
                description = validDescription,
                uploaderId = Id(2u),
                uploadedAt = validDateTime,
            )
        assertEquals(validId, photo.id)
        assertEquals(validHttpsUrl, photo.url)
        assertEquals(validDescription, photo.description)
        assertEquals(Id(2u), photo.uploaderId)
        assertEquals(validDateTime, photo.uploadedAt)
    }

    @Test
    fun `Photo without description should have null description`() {
        val photo =
            Photo(
                id = validId,
                url = validHttpsUrl,
                uploaderId = validId,
                uploadedAt = validDateTime,
            )
        assertEquals(null, photo.description)
    }
}
