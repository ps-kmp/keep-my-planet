package pt.isel.keepmyplanet.repository.database

import pt.isel.keepmyplanet.domain.common.Id
import pt.isel.keepmyplanet.repository.UserDeviceRepository
import pt.isel.keepmyplanet.utils.now
import ptiselkeepmyplanetdb.UserDeviceQueries

class DatabaseUserDeviceRepository(
    private val queries: UserDeviceQueries,
) : UserDeviceRepository {
    override suspend fun addDevice(
        userId: Id,
        token: String,
        platform: String,
    ) {
        queries.transaction {
            queries.deleteByToken(token)
            queries.insert(userId, token, platform, now())
        }
    }

    override suspend fun removeDevice(token: String) {
        queries.deleteByToken(token)
    }

    override suspend fun findTokensByUserId(userId: Id): List<String> =
        queries.findByUserId(userId).executeAsList()
}
