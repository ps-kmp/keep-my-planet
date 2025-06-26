package pt.isel.keepmyplanet.service

import pt.isel.keepmyplanet.domain.common.Id
import pt.isel.keepmyplanet.domain.event.EventStatus
import pt.isel.keepmyplanet.domain.user.Email
import pt.isel.keepmyplanet.domain.user.Name
import pt.isel.keepmyplanet.domain.user.Password
import pt.isel.keepmyplanet.domain.user.User
import pt.isel.keepmyplanet.domain.user.UserStats
import pt.isel.keepmyplanet.exception.AuthorizationException
import pt.isel.keepmyplanet.exception.ConflictException
import pt.isel.keepmyplanet.exception.InternalServerException
import pt.isel.keepmyplanet.exception.NotFoundException
import pt.isel.keepmyplanet.exception.ValidationException
import pt.isel.keepmyplanet.repository.EventRepository
import pt.isel.keepmyplanet.repository.PhotoRepository
import pt.isel.keepmyplanet.repository.UserRepository
import pt.isel.keepmyplanet.security.PasswordHasher
import pt.isel.keepmyplanet.utils.now

class UserService(
    private val userRepository: UserRepository,
    private val eventRepository: EventRepository,
    private val passwordHasher: PasswordHasher,
    private val photoRepository: PhotoRepository,
) {
    suspend fun registerUser(
        name: Name,
        email: Email,
        password: Password,
    ): Result<User> =
        runCatching {
            ensureEmailIsAvailableOrFail(email)
            ensureNameIsAvailableOrFail(name)

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

            if (name != null && name != user.name) {
                ensureNameIsAvailableOrFail(name)
            }

            if (profilePictureId != null && profilePictureId != user.profilePictureId) {
                val photo =
                    photoRepository.getById(profilePictureId)
                        ?: throw ValidationException("Photo with ID '$profilePictureId' not found.")

                if (photo.uploaderId != actingUserId) {
                    throw AuthorizationException("Cannot use a photo that was not uploaded by you.")
                }
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
            val organizedEvents = eventRepository.findByOrganizerId(userId, 1, 0)
            if (organizedEvents.any { it.status in activeEventStatus }) {
                throw ConflictException("Cannot delete user with active organized events.")
            }

            val deleted = userRepository.deleteById(userId)
            if (!deleted) throw InternalServerException("Failed to delete user '$userId'.")
            Unit
        }

    suspend fun getUserStats(
        userId: Id,
        actingUserId: Id,
    ): Result<UserStats> =
        runCatching {
            ensureSelfActionOrFail(userId, actingUserId, "view stats")
            findUserOrFail(userId)
            val totalEvents = eventRepository.countAttendedEvents(userId)
            val totalSeconds = eventRepository.calculateTotalHoursVolunteered(userId)

            val totalHours = totalSeconds / 3600.0

            UserStats(
                totalEventsAttended = totalEvents.toInt(),
                totalHoursVolunteered = totalHours,
            )
        }

    private suspend fun findUserOrFail(userId: Id): User =
        userRepository.getById(userId)
            ?: throw NotFoundException("User '$userId' not found.")

    private suspend fun ensureEmailIsAvailableOrFail(email: Email) {
        if (userRepository.findByEmail(email) != null) {
            throw ConflictException("Email '${email.value}' is already registered.")
        }
    }

    private suspend fun ensureNameIsAvailableOrFail(name: Name) {
        if (userRepository.findByName(name) != null) {
            throw ConflictException("Username '${name.value}' is already taken.")
        }
    }

    private fun ensureSelfActionOrFail(
        targetUserId: Id,
        actingUserId: Id,
        actionDescription: String,
    ) {
        if (targetUserId != actingUserId) {
            throw AuthorizationException(
                "User '$actingUserId' is not authorized to " +
                    "$actionDescription for user '$targetUserId'.",
            )
        }
    }
}
