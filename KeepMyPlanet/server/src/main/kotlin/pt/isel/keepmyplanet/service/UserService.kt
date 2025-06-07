package pt.isel.keepmyplanet.service

import pt.isel.keepmyplanet.domain.common.Id
import pt.isel.keepmyplanet.domain.event.EventStatus
import pt.isel.keepmyplanet.domain.user.Email
import pt.isel.keepmyplanet.domain.user.Name
import pt.isel.keepmyplanet.domain.user.Password
import pt.isel.keepmyplanet.domain.user.User
import pt.isel.keepmyplanet.errors.AuthorizationException
import pt.isel.keepmyplanet.errors.ConflictException
import pt.isel.keepmyplanet.errors.InternalServerException
import pt.isel.keepmyplanet.errors.NotFoundException
import pt.isel.keepmyplanet.errors.ValidationException
import pt.isel.keepmyplanet.repository.EventRepository
import pt.isel.keepmyplanet.repository.UserRepository
import pt.isel.keepmyplanet.util.PasswordHasher
import pt.isel.keepmyplanet.util.now

class UserService(
    private val userRepository: UserRepository,
    private val eventRepository: EventRepository,
    private val passwordHasher: PasswordHasher,
) {
    suspend fun registerUser(
        name: Name,
        email: Email,
        password: Password,
    ): Result<User> =
        runCatching {
            ensureEmailIsAvailableOrFail(email)

            val passwordHash = passwordHasher.hash(password)
            val currentTime = now()
            val newUser =
                User(
                    id = Id(1U),
                    name = name,
                    email = email,
                    passwordHash = passwordHash,
                    profilePictureId = null,
                    createdAt = currentTime,
                    updatedAt = currentTime,
                )
            userRepository.create(newUser)
        }

    suspend fun getUserDetails(userId: Id): Result<User> =
        runCatching {
            findUserOrFail(userId)
        }

    suspend fun getAllUsers(): Result<List<User>> =
        runCatching {
            userRepository.getAll()
        }

    suspend fun updateUserProfile(
        userId: Id,
        actingUserId: Id,
        name: Name?,
        email: Email?,
        profilePictureId: Id?,
    ): Result<User> =
        runCatching {
            val user = findUserOrFail(userId)
            ensureSelfActionOrFail(userId, actingUserId, "update profile")

            if (email != null && email != user.email) {
                ensureEmailIsAvailableOrFail(email)
            }

            val updatedUser =
                user.copy(
                    name = name ?: user.name,
                    email = email ?: user.email,
                    profilePictureId = profilePictureId ?: user.profilePictureId,
                )

            if (updatedUser != user) userRepository.update(updatedUser) else user
        }

    suspend fun changePassword(
        userId: Id,
        actingUserId: Id,
        oldPassword: Password,
        newPassword: Password,
    ): Result<Unit> =
        runCatching {
            val user = findUserOrFail(userId)
            ensureSelfActionOrFail(userId, actingUserId, "change password")

            if (!passwordHasher.verify(oldPassword, user.passwordHash)) {
                throw AuthorizationException("Password verification failed.")
            }

            if (newPassword.value == oldPassword.value) {
                throw ValidationException("New password cannot be the same as the old password.")
            }

            val newPasswordHash = passwordHasher.hash(newPassword)
            val updatedUser = user.copy(passwordHash = newPasswordHash)
            userRepository.update(updatedUser)
            Unit
        }

    suspend fun deleteUser(
        userId: Id,
        actingUserId: Id,
    ): Result<Unit> =
        runCatching {
            findUserOrFail(userId)
            ensureSelfActionOrFail(userId, actingUserId, "delete account")

            val activeEventStatus = setOf(EventStatus.PLANNED, EventStatus.IN_PROGRESS)
            val organizedEvents = eventRepository.findByOrganizerId(userId)
            if (organizedEvents.any { it.status in activeEventStatus }) {
                throw ConflictException("Cannot delete user with active organized events.")
            }

            val deleted = userRepository.deleteById(userId)
            if (!deleted) throw InternalServerException("Failed to delete user '$userId'.")
            Unit
        }

    private suspend fun findUserOrFail(userId: Id): User =
        userRepository.getById(userId)
            ?: throw NotFoundException("User '$userId' not found.")

    private suspend fun ensureEmailIsAvailableOrFail(email: Email) {
        if (userRepository.findByEmail(email) != null) {
            throw ConflictException("Email '${email.value}' is already registered.")
        }
    }

    private fun ensureSelfActionOrFail(
        targetUserId: Id,
        actingUserId: Id,
        actionDescription: String,
    ) {
        if (targetUserId != actingUserId) {
            throw AuthorizationException(
                "User '$actingUserId' is not authorized to $actionDescription for user '$targetUserId'.",
            )
        }
    }
}
