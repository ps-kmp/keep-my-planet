package pt.isel.keepmyplanet.repository.db

import pt.isel.keepmyplanet.domain.common.Id
import pt.isel.keepmyplanet.domain.user.Email
import pt.isel.keepmyplanet.domain.user.Name
import pt.isel.keepmyplanet.domain.user.User
import pt.isel.keepmyplanet.errors.ConflictException
import pt.isel.keepmyplanet.errors.NotFoundException
import pt.isel.keepmyplanet.repository.UserRepository
import pt.isel.keepmyplanet.util.now
import ptiselkeepmyplanetdb.UserQueries
import ptiselkeepmyplanetdb.Users

private fun Users.toDomainUser(): User =
    User(
        id = this.id,
        name = this.name,
        email = this.email,
        passwordHash = this.password_hash,
        profilePictureId = this.profile_picture_id,
        createdAt = this.created_at,
        updatedAt = this.updated_at,
    )

class DatabaseUserRepository(
    private val userQueries: UserQueries,
) : UserRepository {
    override suspend fun create(entity: User): User {
        val currentTime = now()
        return userQueries.transactionWithResult {
            userQueries
                .insert(
                    name = entity.name,
                    email = entity.email,
                    password_hash = entity.passwordHash,
                    profile_picture_id = entity.profilePictureId,
                    created_at = currentTime,
                    updated_at = currentTime,
                ).executeAsOne()
                .toDomainUser()
        }
    }

    override suspend fun getById(id: Id): User? =
        userQueries
            .getById(id)
            .executeAsOneOrNull()
            ?.toDomainUser()

    override suspend fun getAll(
        limit: Int,
        offset: Int,
    ): List<User> =
        userQueries
            .getAll(limit.toLong(), offset.toLong())
            .executeAsList()
            .map { it.toDomainUser() }

    override suspend fun findByIds(ids: List<Id>): List<User> {
        if (ids.isEmpty()) return emptyList()
        return userQueries.findByIds(ids).executeAsList().map { it.toDomainUser() }
    }

    override suspend fun update(entity: User): User {
        val existingUser =
            getById(entity.id)
                ?: throw NotFoundException("User '${entity.id}' not found.")

        if (entity.email != existingUser.email) {
            findByEmail(entity.email)?.let {
                if (it.id != entity.id) {
                    throw ConflictException(
                        "Email '${entity.email}' is already used by another user.",
                    )
                }
            }
        }

        return userQueries.transactionWithResult {
            userQueries
                .updateUser(
                    id = entity.id,
                    name = entity.name,
                    email = entity.email,
                    password_hash = entity.passwordHash,
                    profile_picture_id = entity.profilePictureId,
                    updated_at = now(),
                ).executeAsOne()
                .toDomainUser()
        }
    }

    override suspend fun deleteById(id: Id): Boolean {
        val dbUserExists = userQueries.getById(id).executeAsOneOrNull() != null
        if (dbUserExists) {
            userQueries.deleteById(id)
            return true
        }
        return false
    }

    override suspend fun findByEmail(email: Email): User? =
        userQueries
            .findByEmail(email)
            .executeAsOneOrNull()
            ?.toDomainUser()

    override suspend fun findByName(name: Name): User? =
        userQueries
            .findByName(name)
            .executeAsOneOrNull()
            ?.toDomainUser()
}
