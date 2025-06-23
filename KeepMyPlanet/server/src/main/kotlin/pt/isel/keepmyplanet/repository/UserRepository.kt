package pt.isel.keepmyplanet.repository

import pt.isel.keepmyplanet.domain.common.Id
import pt.isel.keepmyplanet.domain.user.Email
import pt.isel.keepmyplanet.domain.user.Name
import pt.isel.keepmyplanet.domain.user.User

interface UserRepository : Repository<User, Id> {
    suspend fun findByEmail(email: Email): User?

    suspend fun findByName(name: Name): User?

    suspend fun findByIds(ids: List<Id>): List<User>
}
