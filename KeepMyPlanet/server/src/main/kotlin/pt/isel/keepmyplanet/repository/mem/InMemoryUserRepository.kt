package pt.isel.keepmyplanet.repository.mem

import kotlinx.datetime.LocalDateTime
import pt.isel.keepmyplanet.core.DuplicateEmailException
import pt.isel.keepmyplanet.core.NotFoundException
import pt.isel.keepmyplanet.domain.common.Id
import pt.isel.keepmyplanet.domain.user.Email
import pt.isel.keepmyplanet.domain.user.Name
import pt.isel.keepmyplanet.domain.user.User
import pt.isel.keepmyplanet.repository.UserRepository
import pt.isel.keepmyplanet.util.nowUTC
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.atomic.AtomicInteger

class InMemoryUserRepository : UserRepository {
    private val users = ConcurrentHashMap<Id, User>()
    private val nextId = AtomicInteger(1)

    private fun now(): LocalDateTime = LocalDateTime.nowUTC

    override suspend fun create(entity: User): User {
        if (users.values.any { it.email == entity.email }) {
            throw DuplicateEmailException(entity.email)
        }
        val newId = Id(nextId.getAndIncrement().toUInt())
        val currentTime = now()
        val newUser = entity.copy(id = newId, createdAt = currentTime, updatedAt = currentTime)
        users[newId] = newUser
        return newUser
    }

    override suspend fun getById(id: Id): User? = users[id]

    override suspend fun getAll(): List<User> =
        users.values
            .toList()
            .sortedBy { it.id.value }

    override suspend fun update(entity: User): User {
        val existingUser = users[entity.id] ?: throw NotFoundException("User", entity.id)
        if (entity.email != existingUser.email &&
            users.values.any { it.id != entity.id && it.email == entity.email }
        ) {
            throw DuplicateEmailException(entity.email)
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

    fun clear() {
        users.clear()
        nextId.set(1)
    }
}
