package pt.isel.keepmyplanet.api

import io.ktor.client.request.delete
import io.ktor.client.request.get
import io.ktor.client.request.header
import io.ktor.client.request.patch
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.client.statement.bodyAsText
import io.ktor.http.ContentType
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpStatusCode
import io.ktor.http.contentType
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue
import kotlinx.serialization.json.Json
import pt.isel.keepmyplanet.domain.event.EventStatus
import pt.isel.keepmyplanet.domain.user.Email
import pt.isel.keepmyplanet.domain.user.Name
import pt.isel.keepmyplanet.domain.user.Password
import pt.isel.keepmyplanet.dto.auth.ChangePasswordRequest
import pt.isel.keepmyplanet.dto.auth.RegisterRequest
import pt.isel.keepmyplanet.dto.user.UpdateProfileRequest
import pt.isel.keepmyplanet.dto.user.UserResponse
import pt.isel.keepmyplanet.service.UserService

class UserWebApiTest : BaseWebApiTest() {
    private val userService = UserService(fakeUserRepository, fakeEventRepository, passwordHasher)

    @Test
    fun `POST users - should register user successfully`() =
        testApp({ userWebApi(userService) }) {
            val request = RegisterRequest("Test User", "test@example.com", "Password123!")

            val response =
                client.post("/users") {
                    contentType(ContentType.Application.Json)
                    setBody(Json.encodeToString(request))
                }
            assertEquals(HttpStatusCode.Created, response.status)

            val responseBody = Json.decodeFromString<UserResponse>(response.bodyAsText())
            assertEquals(request.name, responseBody.name)
            assertEquals(request.email, responseBody.email)
            assertNotNull(responseBody.id)
            assertNotNull(responseBody.createdAt)
            assertNotNull(responseBody.updatedAt)

            val createdUser = fakeUserRepository.findByEmail(Email(request.email))
            assertNotNull(createdUser)
            assertEquals(request.name, createdUser.name.value)
            assertTrue(passwordHasher.verify(Password(request.password), createdUser.passwordHash))
        }

    @Test
    fun `POST users - should fail with 409 Conflict when email exists`() =
        testApp({ userWebApi(userService) }) {
            createTestUser(email = Email("conflict@example.com"), password = Password("Password1!"))
            val request = RegisterRequest("Another User", "conflict@example.com", "Password123!")

            val response =
                client.post("/users") {
                    contentType(ContentType.Application.Json)
                    setBody(Json.encodeToString(request))
                }
            assertEquals(HttpStatusCode.Conflict, response.status)
        }

    @Test
    fun `POST users - should fail with 400 Bad Request on missing fields`() =
        testApp({ userWebApi(userService) }) {
            val invalidJson = """{"name": "Test User", "password": "Password123!"}"""

            val response =
                client.post("/users") {
                    contentType(ContentType.Application.Json)
                    setBody(invalidJson)
                }
            assertEquals(HttpStatusCode.BadRequest, response.status)
        }

    @Test
    fun `POST users - should fail with 400 Bad Request on invalid email format`() =
        testApp({ userWebApi(userService) }) {
            val request = RegisterRequest("Test User", "invalid-email", "Password123!")

            val response =
                client.post("/users") {
                    contentType(ContentType.Application.Json)
                    setBody(Json.encodeToString(request))
                }
            assertEquals(HttpStatusCode.BadRequest, response.status)
        }

    @Test
    fun `GET users - should return list of existing users`() =
        testApp({ userWebApi(userService) }) {
            val user1 = createTestUser(name = Name("User One"), email = Email("one@test.com"))
            val user2 = createTestUser(name = Name("User Two"), email = Email("two@test.com"))
            val user3 = createTestUser(name = Name("User Three"), email = Email("three@test.com"))

            val response = client.get("/users")
            assertEquals(HttpStatusCode.OK, response.status)

            val responseList = Json.decodeFromString<List<UserResponse>>(response.bodyAsText())
            assertEquals(3, responseList.size)
            assertTrue(responseList.any { it.id == user1.id.value && it.name == "User One" })
            assertTrue(responseList.any { it.id == user2.id.value && it.name == "User Two" })
            assertTrue(responseList.any { it.id == user3.id.value && it.name == "User Three" })
        }

    @Test
    fun `GET users by ID - should return user when found`() =
        testApp({ userWebApi(userService) }) {
            val user = createTestUser(name = Name("Detail User"), email = Email("detail@test.com"))

            val response = client.get("/users/${user.id.value}")
            assertEquals(HttpStatusCode.OK, response.status)

            val responseBody = Json.decodeFromString<UserResponse>(response.bodyAsText())
            assertEquals(user.id.value, responseBody.id)
            assertEquals(user.name.value, responseBody.name)
            assertEquals(user.email.value, responseBody.email)
        }

    @Test
    fun `GET users by ID - should return 404 when not found`() =
        testApp({ userWebApi(userService) }) {
            val nonExistentId = 9999U
            val response = client.get("/users/$nonExistentId")

            assertEquals(HttpStatusCode.NotFound, response.status)
        }

    @Test
    fun `GET users by ID - should return 400 for invalid ID format`() =
        testApp({ userWebApi(userService) }) {
            val response = client.get("/users/invalid-id-format")
            assertEquals(HttpStatusCode.BadRequest, response.status)
        }

    @Test
    fun `PATCH users by ID - should update profile successfully`() =
        testApp({ userWebApi(userService) }) {
            val userToUpdate = createTestUser(Name("Old Name"), Email("old@test.com"))
            val request = UpdateProfileRequest(name = "New Name", email = "new@test.com")
            val token = generateTestToken(userToUpdate.id)

            val response =
                client.patch("/users/${userToUpdate.id.value}") {
                    header(HttpHeaders.Authorization, "Bearer $token")
                    contentType(ContentType.Application.Json)
                    setBody(Json.encodeToString(request))
                }
            assertEquals(HttpStatusCode.OK, response.status)

            val responseBody = Json.decodeFromString<UserResponse>(response.bodyAsText())
            assertEquals(userToUpdate.id.value, responseBody.id)
            assertEquals("New Name", responseBody.name)
            assertEquals("new@test.com", responseBody.email)

            val updatedUser = fakeUserRepository.getById(userToUpdate.id)
            assertNotNull(updatedUser)
            assertEquals(Name("New Name"), updatedUser.name)
            assertEquals(Email("new@test.com"), updatedUser.email)
        }

    @Test
    fun `PATCH users by ID - should fail with 404 when user not found`() =
        testApp({ userWebApi(userService) }) {
            val actingUser = createTestUser()
            val nonExistentId = 9999U
            val request = UpdateProfileRequest(name = "Name")
            val token = generateTestToken(actingUser.id)

            val response =
                client.patch("/users/$nonExistentId") {
                    header(HttpHeaders.Authorization, "Bearer $token")
                    contentType(ContentType.Application.Json)
                    setBody(Json.encodeToString(request))
                }
            assertEquals(HttpStatusCode.NotFound, response.status)
        }

    @Test
    fun `PATCH users by ID - should fail with 403 Forbidden when not updating self`() =
        testApp({ userWebApi(userService) }) {
            val userToUpdate = createTestUser(email = Email("target@test.com"))
            val actingUser = createTestUser(email = Email("actor@test.com"))
            val request = UpdateProfileRequest(name = "Attempted Name")
            val token = generateTestToken(actingUser.id)

            val response =
                client.patch("/users/${userToUpdate.id.value}") {
                    header(HttpHeaders.Authorization, "Bearer $token")
                    contentType(ContentType.Application.Json)
                    setBody(Json.encodeToString(request))
                }
            assertEquals(HttpStatusCode.Forbidden, response.status)
        }

    @Test
    fun `PATCH users by ID - should fail with 401 Unauthorized when no auth token`() =
        testApp({ userWebApi(userService) }) {
            val userToUpdate = createTestUser()
            val request = UpdateProfileRequest(name = "New Name")

            val response =
                client.patch("/users/${userToUpdate.id.value}") {
                    // No Authorization header
                    contentType(ContentType.Application.Json)
                    setBody(Json.encodeToString(request))
                }
            assertEquals(HttpStatusCode.Unauthorized, response.status)
        }

    @Test
    fun `PATCH users by ID - should fail with 409 Conflict when email is taken`() =
        testApp({ userWebApi(userService) }) {
            val user1 = createTestUser(email = Email("user1@test.com"))
            createTestUser(email = Email("user2@test.com"))
            val request = UpdateProfileRequest(email = "user2@test.com")
            val token = generateTestToken(user1.id)

            val response =
                client.patch("/users/${user1.id.value}") {
                    header(HttpHeaders.Authorization, "Bearer $token")
                    contentType(ContentType.Application.Json)
                    setBody(Json.encodeToString(request))
                }
            assertEquals(HttpStatusCode.Conflict, response.status)
        }

    @Test
    fun `PATCH users by ID - should fail with 400 Bad Request for invalid ID format`() =
        testApp({ userWebApi(userService) }) {
            val actingUser = createTestUser()
            val request = UpdateProfileRequest(name = "Any Name")
            val token = generateTestToken(actingUser.id)

            val response =
                client.patch("/users/invalid-id") {
                    header(HttpHeaders.Authorization, "Bearer $token")
                    contentType(ContentType.Application.Json)
                    setBody(Json.encodeToString(request))
                }
            assertEquals(HttpStatusCode.BadRequest, response.status)
        }

    @Test
    fun `DELETE users by ID - should delete user successfully`() =
        testApp({ userWebApi(userService) }) {
            val userToDelete = createTestUser()
            val userId = userToDelete.id
            val token = generateTestToken(userId)

            val response =
                client.delete("/users/${userId.value}") {
                    header(HttpHeaders.Authorization, "Bearer $token")
                }
            assertEquals(HttpStatusCode.NoContent, response.status)
            assertNull(fakeUserRepository.getById(userId))
        }

    @Test
    fun `DELETE users by ID - should fail with 404 when user not found`() =
        testApp({ userWebApi(userService) }) {
            val actingUser = createTestUser()
            val nonExistentId = 9999U
            val token = generateTestToken(actingUser.id)

            val response =
                client.delete("/users/$nonExistentId") {
                    header(HttpHeaders.Authorization, "Bearer $token")
                }
            assertEquals(HttpStatusCode.NotFound, response.status)
        }

    @Test
    fun `DELETE users by ID - should fail with 403 Forbidden when not deleting self`() =
        testApp({ userWebApi(userService) }) {
            val userToDelete = createTestUser(email = Email("target@delete.com"))
            val actingUser = createTestUser(email = Email("actor@delete.com"))
            val token = generateTestToken(actingUser.id)

            val response =
                client.delete("/users/${userToDelete.id.value}") {
                    header(HttpHeaders.Authorization, "Bearer $token")
                }
            assertEquals(HttpStatusCode.Forbidden, response.status)
            assertNotNull(fakeUserRepository.getById(userToDelete.id))
        }

    @Test
    fun `DELETE users by ID - should fail with 401 Unauthorized when no auth token`() =
        testApp({ userWebApi(userService) }) {
            val userToDelete = createTestUser()

            val response =
                client.delete("/users/${userToDelete.id.value}") {
                    // No Authorization header
                }
            assertEquals(HttpStatusCode.Unauthorized, response.status)
            assertNotNull(fakeUserRepository.getById(userToDelete.id))
        }

    @Test
    fun `DELETE users by ID - should fail with 409 Conflict when user has active events`() =
        testApp({ userWebApi(userService) }) {
            val organizer = createTestUser()
            val zone = createTestZone(reporterId = organizer.id)
            val event = createTestEvent(zone.id, organizer.id, status = EventStatus.PLANNED)
            linkEventToZone(zone.id, event.id)
            val token = generateTestToken(organizer.id)

            val response =
                client.delete("/users/${organizer.id.value}") {
                    header(HttpHeaders.Authorization, "Bearer $token")
                }
            assertEquals(HttpStatusCode.Conflict, response.status)
            assertNotNull(fakeUserRepository.getById(organizer.id))
        }

    @Test
    fun `DELETE users by ID - should fail with 400 Bad Request for invalid ID format`() =
        testApp({ userWebApi(userService) }) {
            val actingUser = createTestUser()
            val token = generateTestToken(actingUser.id)

            val response =
                client.delete("/users/invalid-id") {
                    header(HttpHeaders.Authorization, "Bearer $token")
                }
            assertEquals(HttpStatusCode.BadRequest, response.status)
        }

    @Test
    fun `PATCH users password - should change password successfully`() =
        testApp({ userWebApi(userService) }) {
            val oldPassword = "OldPassword1!"
            val newPassword = "NewPassword1!"
            val user = createTestUser(password = Password(oldPassword))
            val request = ChangePasswordRequest(oldPassword, newPassword)
            val token = generateTestToken(user.id)

            val response =
                client.patch("/users/${user.id.value}/password") {
                    header(HttpHeaders.Authorization, "Bearer $token")
                    contentType(ContentType.Application.Json)
                    setBody(Json.encodeToString(request))
                }
            assertEquals(HttpStatusCode.OK, response.status)

            val updatedUser = fakeUserRepository.getById(user.id)
            assertNotNull(updatedUser)
            assertTrue(passwordHasher.verify(Password(newPassword), updatedUser.passwordHash))
            assertFalse(passwordHasher.verify(Password(oldPassword), updatedUser.passwordHash))
        }

    @Test
    fun `PATCH users password - should fail with 404 when user not found`() =
        testApp({ userWebApi(userService) }) {
            val actingUser = createTestUser()
            val nonExistentId = 9999U
            val request = ChangePasswordRequest("Password1!", "NewPassword1!")
            val token = generateTestToken(actingUser.id)

            val response =
                client.patch("/users/$nonExistentId/password") {
                    header(HttpHeaders.Authorization, "Bearer $token")
                    contentType(ContentType.Application.Json)
                    setBody(Json.encodeToString(request))
                }
            assertEquals(HttpStatusCode.NotFound, response.status)
        }

    @Test
    fun `PATCH users password - should fail with 403 Forbidden when not changing self`() =
        testApp({ userWebApi(userService) }) {
            val oldPassword = "OldPassword1!"
            val newPassword = "NewPassword1!"
            val userToUpdate = createTestUser(password = Password(oldPassword))
            val actingUser = createTestUser(email = Email("actor@pass.com"))
            val request = ChangePasswordRequest(oldPassword, newPassword)
            val token = generateTestToken(actingUser.id)

            val response =
                client.patch("/users/${userToUpdate.id.value}/password") {
                    // Path for userToUpdate
                    header(HttpHeaders.Authorization, "Bearer $token")
                    contentType(ContentType.Application.Json)
                    setBody(Json.encodeToString(request))
                }
            assertEquals(HttpStatusCode.Forbidden, response.status)

            val notUpdatedUser = fakeUserRepository.getById(userToUpdate.id)
            assertNotNull(notUpdatedUser)
            // Verify password hash is still the old one
            assertTrue(passwordHasher.verify(Password(oldPassword), notUpdatedUser.passwordHash))
        }

    @Test
    fun `PATCH users password - should fail with 403 Forbidden on incorrect old password`() =
        testApp({ userWebApi(userService) }) {
            val oldPassword = "OldPassword1!"
            val newPassword = "NewPassword1!"
            val user = createTestUser(password = Password(oldPassword))
            val request = ChangePasswordRequest("WRONG_OLD_PASSWORDa1!", newPassword)
            val token = generateTestToken(user.id)

            val response =
                client.patch("/users/${user.id.value}/password") {
                    header(HttpHeaders.Authorization, "Bearer $token")
                    contentType(ContentType.Application.Json)
                    setBody(Json.encodeToString(request))
                }
            assertEquals(HttpStatusCode.Forbidden, response.status)

            val notUpdatedUser = fakeUserRepository.getById(user.id)
            assertNotNull(notUpdatedUser)
            assertTrue(passwordHasher.verify(Password(oldPassword), notUpdatedUser.passwordHash))
        }

    @Test
    fun `PATCH users password - should fail with 401 Unauthorized when no auth token`() =
        testApp({ userWebApi(userService) }) {
            val oldPassword = "OldPassword1!"
            val newPassword = "NewPassword1!"
            val user = createTestUser(password = Password(oldPassword))
            val request = ChangePasswordRequest(oldPassword, newPassword)

            val response =
                client.patch("/users/${user.id.value}/password") {
                    // No Authorization header
                    contentType(ContentType.Application.Json)
                    setBody(Json.encodeToString(request))
                }
            assertEquals(HttpStatusCode.Unauthorized, response.status)
        }

    @Test
    fun `PATCH users password - should fail with 400 Bad Request when new password is the old`() =
        testApp({ userWebApi(userService) }) {
            val oldPassword = "OldPassword1!"
            val user = createTestUser(password = Password(oldPassword))
            val request = ChangePasswordRequest(oldPassword, oldPassword)
            val token = generateTestToken(user.id)

            val response =
                client.patch("/users/${user.id.value}/password") {
                    header(HttpHeaders.Authorization, "Bearer $token")
                    contentType(ContentType.Application.Json)
                    setBody(Json.encodeToString(request))
                }
            assertEquals(HttpStatusCode.BadRequest, response.status)
        }

    @Test
    fun `PATCH users password - should fail with 400 Bad Request for invalid ID format`() =
        testApp({ userWebApi(userService) }) {
            val actingUser = createTestUser()
            val request = ChangePasswordRequest("anyPassword1!", "newPassword1!")
            val token = generateTestToken(actingUser.id)

            val response =
                client.patch("/users/invalid-id/password") {
                    header(HttpHeaders.Authorization, "Bearer $token")
                    contentType(ContentType.Application.Json)
                    setBody(Json.encodeToString(request))
                }
            assertEquals(HttpStatusCode.BadRequest, response.status)
        }

    @Test
    fun `PATCH users password - should fail with 400 Bad Request on missing fields`() =
        testApp({ userWebApi(userService) }) {
            val user = createTestUser()
            val invalidJson = """{"oldPassword": "somePassword"}"""
            val token = generateTestToken(user.id)

            val response =
                client.patch("/users/${user.id.value}/password") {
                    header(HttpHeaders.Authorization, "Bearer $token")
                    contentType(ContentType.Application.Json)
                    setBody(invalidJson)
                }
            assertEquals(HttpStatusCode.BadRequest, response.status)
        }
}
