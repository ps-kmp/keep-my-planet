package pt.isel.keepmyplanet.repository.memory

import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.atomic.AtomicInteger
import pt.isel.keepmyplanet.domain.common.Id
import pt.isel.keepmyplanet.domain.user.Email
import pt.isel.keepmyplanet.domain.user.Name
import pt.isel.keepmyplanet.domain.user.User
import pt.isel.keepmyplanet.domain.user.UserRole
import pt.isel.keepmyplanet.exception.ConflictException
import pt.isel.keepmyplanet.exception.NotFoundException
import pt.isel.keepmyplanet.repository.UserRepository
import pt.isel.keepmyplanet.utils.now

class InMemoryUserRepository : UserRepository {
    private val users = ConcurrentHashMap<Id, User>()
    private val nextId = AtomicInteger(1)

    override suspend fun create(entity: User): User {
        if (users.values.any { it.email == entity.email }) {
            throw ConflictException("User with email '${entity.email}' already exists.")
        }
        val newId = Id(nextId.getAndIncrement().toUInt())
        val currentTime = now()
        val newUser = entity.copy(id = newId, createdAt = currentTime, updatedAt = currentTime)
        users[newId] = newUser
        return newUser
    }

    override suspend fun getById(id: Id): User? = users[id]

    override suspend fun getAll(
        limit: Int,
        offset: Int,
    ): List<User> =
        users.values
            .toList()
            .sortedBy { it.id.value }
            .drop(offset)
            .take(limit)

    override suspend fun update(entity: User): User {
        val existingUser =
            users[entity.id] ?: throw NotFoundException("User '${entity.id}' not found.")
        if (entity.email != existingUser.email &&
            users.values.any { it.id != entity.id && it.email == entity.email }
        ) {
            throw ConflictException("Email '${entity.email}' is already used.")
        }
        val updatedUser = entity.copy(createdAt = existingUser.createdAt, updatedAt = now())
        users[entity.id] = updatedUser
        return updatedUser
    }

    override suspend fun deleteById(id: Id): Boolean = users.remove(id) != null

    override suspend fun findByEmail(email: Email): User? =
        users.values
            .find { it.email == email }

    override suspend fun findByName(name: Name): User? =
        users.values
            .find { it.name == name }

    override suspend fun findByIds(ids: List<Id>): List<User> =
        if (ids.isEmpty()) {
            emptyList()
        } else {
            users.filterKeys { it in ids }.values.toList()
        }

    override suspend fun updateUserRole(
        userId: Id,
        newRole: UserRole,
    ): User {
        val user = getById(userId) ?: throw NotFoundException("User '$userId' not found.")
        val updatedUser = user.copy(role = newRole, updatedAt = now())
        users[userId] = updatedUser
        return updatedUser
    }

    fun clear() {
        users.clear()
        nextId.set(1)
    }
}
