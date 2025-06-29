package pt.isel.keepmyplanet.repository

import pt.isel.keepmyplanet.domain.common.Id

interface UserDeviceRepository {
    suspend fun addDevice(
        userId: Id,
        token: String,
        platform: String,
    )

    suspend fun removeDevice(token: String)

    suspend fun findTokensByUserId(userId: Id): List<String>
}
