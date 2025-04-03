package pt.isel.keepmyplanet.repository.mem

import kotlinx.datetime.LocalDateTime
import pt.isel.keepmyplanet.domain.common.Id
import pt.isel.keepmyplanet.domain.user.Email
import pt.isel.keepmyplanet.domain.user.User
import pt.isel.keepmyplanet.repository.UserRepository
import pt.isel.keepmyplanet.services.DuplicateEmailException
import pt.isel.keepmyplanet.services.EmailConflictException
import pt.isel.keepmyplanet.services.UserNotFoundException
import pt.isel.keepmyplanet.utils.nowUTC
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.atomic.AtomicInteger

class InMemoryUserRepository : UserRepository {
    private val usersById = ConcurrentHashMap<Id, User>()
    private val idCounter = AtomicInteger(1)

    private fun generateNewId(): Id = Id(idCounter.getAndIncrement().toUInt())

    override suspend fun create(entity: User): User {
        if (usersById.values.any { it.email == entity.email }) {
            throw DuplicateEmailException(entity.email)
        }
        val now = LocalDateTime.nowUTC
        val userToCreate = entity.copy(id = generateNewId(), createdAt = now, updatedAt = now)
        usersById[userToCreate.id] = userToCreate
        return userToCreate
    }

    override suspend fun getById(id: Id): User? = usersById[id]

    override suspend fun findByEmail(email: Email): User? = usersById.values.find { it.email == email }

    override suspend fun getAll(): List<User> = usersById.values.toList()

    override suspend fun update(entity: User): User {
        val existingUser = usersById[entity.id] ?: throw UserNotFoundException(entity.id)
        val newEmail = entity.email
        if (newEmail != existingUser.email) {
            if (usersById.values.find { it.email == newEmail && it.id != entity.id } != null) {
                throw EmailConflictException(newEmail)
            }
        }
        val updatedUser =
            entity.copy(createdAt = existingUser.createdAt, updatedAt = LocalDateTime.nowUTC)
        usersById[updatedUser.id] = updatedUser
        return updatedUser
    }

    override suspend fun deleteById(id: Id): Boolean = usersById.remove(id) != null

    override suspend fun updateProfilePicture(
        userId: Id,
        profilePictureId: Id?,
    ): User {
        val existingUser = usersById[userId] ?: throw UserNotFoundException(userId)
        val updatedUser =
            existingUser.copy(
                profilePictureId = profilePictureId,
                updatedAt = LocalDateTime.nowUTC,
            )
        usersById[userId] = updatedUser
        return updatedUser
    }

    fun clear() {
        usersById.clear()
        idCounter.set(1)
    }
}
