package pt.isel.keepmyplanet.repository.memory

import java.util.concurrent.ConcurrentHashMap
import pt.isel.keepmyplanet.domain.common.Id
import pt.isel.keepmyplanet.repository.UserDeviceRepository

class InMemoryUserDeviceRepository : UserDeviceRepository {
    private val devices = ConcurrentHashMap<Id, MutableSet<String>>()
    private val tokenToUser = ConcurrentHashMap<String, Id>()

    override suspend fun addDevice(
        userId: Id,
        token: String,
        platform: String,
    ) {
        tokenToUser[token]?.let { devices[it]?.remove(token) }
        devices.computeIfAbsent(userId) { ConcurrentHashMap.newKeySet() }.add(token)
        tokenToUser[token] = userId
    }

    override suspend fun removeDevice(token: String) {
        tokenToUser.remove(token)?.let {
            devices[it]?.remove(token)
            if (devices[it]?.isEmpty() == true) devices.remove(it)
        }
    }

    override suspend fun findTokensByUserId(userId: Id): List<String> =
        devices[userId]?.toList() ?: emptyList()

    fun clear() {
        devices.clear()
        tokenToUser.clear()
    }
}
