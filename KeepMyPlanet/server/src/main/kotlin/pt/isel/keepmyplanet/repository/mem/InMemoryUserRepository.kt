package pt.isel.keepmyplanet.repository.mem

import pt.isel.keepmyplanet.domain.common.Id
import pt.isel.keepmyplanet.domain.user.Email
import pt.isel.keepmyplanet.domain.user.Name
import pt.isel.keepmyplanet.domain.user.PasswordInfo
import pt.isel.keepmyplanet.domain.user.User
import pt.isel.keepmyplanet.errors.ConflictException
import pt.isel.keepmyplanet.errors.NotFoundException
import pt.isel.keepmyplanet.repository.UserRepository
import pt.isel.keepmyplanet.util.now
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.atomic.AtomicInteger

class InMemoryUserRepository : UserRepository {
    private val users = ConcurrentHashMap<Id, User>()
    private val nextId = AtomicInteger(1)

    init {
        val now = now()
        val testUser1 =
            User(
                id = Id(1U),
                name = Name("rafael"),
                email = Email("rafael@example.com"),
                passwordInfo = PasswordInfo("Password1!"),
                createdAt = now,
                updatedAt = now,
            )
        val testUser2 =
            User(
                id = Id(2U),
                name = Name("diogo"),
                email = Email("diogo@example.com"),
                passwordInfo = PasswordInfo("Password1!"),
                createdAt = now,
                updatedAt = now,
            )
        val testUser3 =
            User(
                id = Id(3U),
                name = Name("user"),
                email = Email("user@example.com"),
                passwordInfo = PasswordInfo("Password1!"),
                createdAt = now,
                updatedAt = now,
            )
        users[testUser1.id] = testUser1
        users[testUser2.id] = testUser2
        users[testUser3.id] = testUser3
    }

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

    override suspend fun getAll(): List<User> =
        users.values
            .toList()
            .sortedBy { it.id.value }

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

    fun clear() {
        users.clear()
        nextId.set(1)
    }
}
